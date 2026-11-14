package com.example.data

import android.util.Xml
import com.example.data.local.AccountEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.DebtEntity
import com.example.data.local.TxnEntity
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class ImportedWorkbook(
    val accounts: List<AccountEntity>,
    val txns: List<TxnEntity>,
    val debts: List<DebtEntity>,
    val categories: List<CategoryEntity>?
)

/**
 * Pure-Kotlin OOXML (.xlsx) reader and writer compatible with SheetJS (xlsx) backups
 * from the original web app as well as Excel / Google Sheets.
 */
object XlsxCodec {

    fun writeWorkbook(
        out: OutputStream,
        accounts: List<AccountEntity>,
        txns: List<TxnEntity>,
        debts: List<DebtEntity>,
        categories: List<CategoryEntity>
    ) {
        val sheets = listOf(
            "Accounts" to buildAccountsRows(accounts),
            "Transactions" to buildTxnRows(txns),
            "Debts" to buildDebtRows(debts),
            "Categories" to buildCategoryRows(categories)
        )

        ZipOutputStream(out).use { zip ->
            writeZipEntry(zip, "[Content_Types].xml", contentTypesXml(sheets.size))
            writeZipEntry(zip, "_rels/.rels", rootRelsXml())
            writeZipEntry(zip, "xl/workbook.xml", workbookXml(sheets.map { it.first }))
            writeZipEntry(zip, "xl/_rels/workbook.xml.rels", workbookRelsXml(sheets.size))
            sheets.forEachIndexed { index, (_, rows) ->
                writeZipEntry(zip, "xl/worksheets/sheet${index + 1}.xml", sheetXml(rows))
            }
        }
    }

    fun readWorkbook(input: InputStream): ImportedWorkbook {
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(input).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val buf = ByteArrayOutputStream()
                    zis.copyTo(buf)
                    entries[entry.name.trimStart('/')] = buf.toByteArray()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        val sharedStrings = entries["xl/sharedStrings.xml"]?.let { parseSharedStrings(it) } ?: emptyList()
        val workbookBytes = entries["xl/workbook.xml"]
            ?: throw IllegalArgumentException("সঠিক এক্সেল ফাইল নয়")
        val relsBytes = entries["xl/_rels/workbook.xml.rels"]

        val sheetTargets = resolveSheetTargets(workbookBytes, relsBytes)
        if (!sheetTargets.containsKey("Accounts") ||
            !sheetTargets.containsKey("Transactions") ||
            !sheetTargets.containsKey("Debts")
        ) {
            throw IllegalArgumentException("সঠিক ফাইল নয় (Accounts, Transactions, Debts শিট প্রয়োজন)")
        }

        val accRows = parseSheetTable(
            entries[sheetTargets["Accounts"]] ?: throw IllegalArgumentException("Accounts শিট পাওয়া যায়নি"),
            sharedStrings
        )
        val txnRows = parseSheetTable(
            entries[sheetTargets["Transactions"]] ?: throw IllegalArgumentException("Transactions শিট পাওয়া যায়নি"),
            sharedStrings
        )
        val debtRows = parseSheetTable(
            entries[sheetTargets["Debts"]] ?: throw IllegalArgumentException("Debts শিট পাওয়া যায়নি"),
            sharedStrings
        )
        val catRows = sheetTargets["Categories"]?.let { path ->
            entries[path]?.let { parseSheetTable(it, sharedStrings) }
        }

        val accounts = accRows.mapIndexed { idx, r ->
            AccountEntity(
                id = r["id"]?.toLongOrNull() ?: (idx + 1L),
                name = r["name"].orEmpty(),
                type = r["type"]?.ifBlank { "bank" } ?: "bank",
                opening = r["opening"]?.toDoubleOrNull() ?: 0.0,
                icon = r["icon"].orEmpty()
            )
        }

        val fallbackAccId = accounts.firstOrNull()?.id ?: 1L
        val txns = txnRows.mapIndexed { idx, r ->
            val rawType = r["type"]?.lowercase()
            TxnEntity(
                id = r["id"]?.toLongOrNull() ?: (idx + 1L),
                type = if (rawType == "credit") "credit" else "debit",
                amount = r["amount"]?.toDoubleOrNull() ?: 0.0,
                accountId = (r["account_id"] ?: r["accountId"])?.toLongOrNull() ?: fallbackAccId,
                category = r["category"].orEmpty(),
                note = r["note"].orEmpty(),
                date = r["date"]?.ifBlank { Instant.now().toString() } ?: Instant.now().toString(),
                debtId = (r["debt_id"] ?: r["debtId"])?.takeIf { it.isNotBlank() }?.toLongOrNull(),
                isPayment = parseBoolFlag(r["is_payment"] ?: r["isPayment"]),
                ref = r["ref"]?.takeIf { it.isNotBlank() }
            )
        }

        val debts = debtRows.mapIndexed { idx, r ->
            val rawKind = r["kind"]?.lowercase()
            val kind = when (rawKind) {
                "loan" -> "loan"
                "lent" -> "lent"
                else -> "shop"
            }
            DebtEntity(
                id = r["id"]?.toLongOrNull() ?: (idx + 1L),
                kind = kind,
                person = r["person"].orEmpty(),
                total = r["total"]?.toDoubleOrNull() ?: 0.0,
                note = r["note"].orEmpty(),
                date = r["date"]?.ifBlank { Instant.now().toString() } ?: Instant.now().toString(),
                isExpense = parseBoolFlag(r["is_expense"] ?: r["isExpense"]),
                category = r["category"].orEmpty()
            )
        }

        val categories = catRows?.takeIf { it.isNotEmpty() }?.mapIndexed { idx, r ->
            CategoryEntity(
                id = r["id"]?.toLongOrNull() ?: (idx + 1L),
                name = r["name"].orEmpty(),
                kind = if (r["kind"]?.lowercase() == "income") "income" else "expense"
            )
        }

        return ImportedWorkbook(
            accounts = accounts,
            txns = txns,
            debts = debts,
            categories = categories
        )
    }

    private fun parseBoolFlag(value: String?): Boolean {
        val v = value?.trim()?.lowercase() ?: return false
        return v == "1" || v == "1.0" || v == "true"
    }

    private sealed interface CellVal {
        data class Str(val value: String) : CellVal
        data class Num(val value: Number) : CellVal
    }

    private fun buildAccountsRows(accounts: List<AccountEntity>): List<List<CellVal>> {
        val header = listOf("id", "name", "type", "opening", "icon").map { CellVal.Str(it) }
        val body = accounts.map { a ->
            listOf(
                CellVal.Num(a.id),
                CellVal.Str(a.name),
                CellVal.Str(a.type),
                CellVal.Num(a.opening),
                CellVal.Str(a.icon)
            )
        }
        return listOf(header) + body
    }

    private fun buildTxnRows(txns: List<TxnEntity>): List<List<CellVal>> {
        val header = listOf(
            "id", "type", "amount", "account_id", "category",
            "note", "date", "debt_id", "is_payment", "ref"
        ).map { CellVal.Str(it) }
        val body = txns.map { t ->
            listOf(
                CellVal.Num(t.id),
                CellVal.Str(t.type),
                CellVal.Num(t.amount),
                CellVal.Num(t.accountId),
                CellVal.Str(t.category),
                CellVal.Str(t.note),
                CellVal.Str(t.date),
                if (t.debtId != null) CellVal.Num(t.debtId) else CellVal.Str(""),
                CellVal.Num(if (t.isPayment) 1 else 0),
                CellVal.Str(t.ref.orEmpty())
            )
        }
        return listOf(header) + body
    }

    private fun buildDebtRows(debts: List<DebtEntity>): List<List<CellVal>> {
        val header = listOf(
            "id", "kind", "person", "total", "note", "date", "is_expense", "category"
        ).map { CellVal.Str(it) }
        val body = debts.map { d ->
            listOf(
                CellVal.Num(d.id),
                CellVal.Str(d.kind),
                CellVal.Str(d.person),
                CellVal.Num(d.total),
                CellVal.Str(d.note),
                CellVal.Str(d.date),
                CellVal.Num(if (d.isExpense) 1 else 0),
                CellVal.Str(d.category)
            )
        }
        return listOf(header) + body
    }

    private fun buildCategoryRows(categories: List<CategoryEntity>): List<List<CellVal>> {
        val header = listOf("id", "name", "kind").map { CellVal.Str(it) }
        val body = categories.map { c ->
            listOf(
                CellVal.Num(c.id),
                CellVal.Str(c.name),
                CellVal.Str(c.kind)
            )
        }
        return listOf(header) + body
    }

    private fun writeZipEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(StandardCharsets.UTF_8))
        zip.closeEntry()
    }

    private fun contentTypesXml(sheetCount: Int): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">""")
        append("""<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>""")
        append("""<Default Extension="xml" ContentType="application/xml"/>""")
        append("""<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>""")
        for (i in 1..sheetCount) {
            append("""<Override PartName="/xl/worksheets/sheet$i.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>""")
        }
        append("""</Types>""")
    }

    private fun rootRelsXml(): String =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""" +
            """<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""" +
            """<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>""" +
            """</Relationships>"""

    private fun workbookXml(sheetNames: List<String>): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">""")
        append("""<sheets>""")
        sheetNames.forEachIndexed { idx, name ->
            append("""<sheet name="${escapeXml(name)}" sheetId="${idx + 1}" r:id="rId${idx + 1}"/>""")
        }
        append("""</sheets></workbook>""")
    }

    private fun workbookRelsXml(sheetCount: Int): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
        for (i in 1..sheetCount) {
            append("""<Relationship Id="rId$i" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet$i.xml"/>""")
        }
        append("""</Relationships>""")
    }

    private fun sheetXml(rows: List<List<CellVal>>): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")
        rows.forEachIndexed { rowIdx, row ->
            val rNum = rowIdx + 1
            append("""<row r="$rNum">""")
            row.forEachIndexed { colIdx, cell ->
                val ref = "${colLetter(colIdx)}$rNum"
                when (cell) {
                    is CellVal.Num -> append("""<c r="$ref" t="n"><v>${cell.value}</v></c>""")
                    is CellVal.Str -> append("""<c r="$ref" t="inlineStr"><is><t xml:space="preserve">${escapeXml(cell.value)}</t></is></c>""")
                }
            }
            append("""</row>""")
        }
        append("""</sheetData></worksheet>""")
    }

    private fun colLetter(index: Int): String {
        var n = index
        val sb = StringBuilder()
        while (n >= 0) {
            sb.insert(0, ('A' + (n % 26)))
            n = (n / 26) - 1
        }
        return sb.toString()
    }

    private fun escapeXml(s: String): String = buildString(s.length) {
        for (ch in s) {
            when (ch) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&apos;")
                else -> append(ch)
            }
        }
    }

    private fun parseSharedStrings(bytes: ByteArray): List<String> {
        val result = mutableListOf<String>()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(ByteArrayInputStream(bytes), "UTF-8")

        var inSi = false
        var inT = false
        val current = StringBuilder()

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (localName(parser.name)) {
                        "si" -> {
                            inSi = true
                            current.clear()
                        }
                        "t" -> if (inSi) inT = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inSi && inT) current.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (localName(parser.name)) {
                        "t" -> inT = false
                        "si" -> {
                            inSi = false
                            result.add(current.toString())
                        }
                    }
                }
            }
            event = parser.next()
        }
        return result
    }

    private fun resolveSheetTargets(
        workbookBytes: ByteArray,
        relsBytes: ByteArray?
    ): Map<String, String> {
        val relMap = mutableMapOf<String, String>()
        if (relsBytes != null) {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(ByteArrayInputStream(relsBytes), "UTF-8")
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && localName(parser.name) == "Relationship") {
                    val id = parser.getAttributeValue(null, "Id")
                    val target = parser.getAttributeValue(null, "Target")
                    if (!id.isNullOrBlank() && !target.isNullOrBlank()) {
                        val normalized = target.trimStart('/')
                        relMap[id] = if (normalized.startsWith("xl/")) normalized else "xl/$normalized"
                    }
                }
                event = parser.next()
            }
        }

        val sheets = mutableMapOf<String, String>()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(ByteArrayInputStream(workbookBytes), "UTF-8")
        var idx = 1
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && localName(parser.name) == "sheet") {
                val name = parser.getAttributeValue(null, "name")
                val rId = parser.getAttributeValue(null, "r:id")
                    ?: parser.getAttributeValue(null, "id")
                val target = rId?.let { relMap[it] } ?: "xl/worksheets/sheet$idx.xml"
                if (!name.isNullOrBlank()) {
                    sheets[name] = target
                }
                idx++
            }
            event = parser.next()
        }
        return sheets
    }

    private fun parseSheetTable(
        bytes: ByteArray,
        sharedStrings: List<String>
    ): List<Map<String, String>> {
        val rows = mutableListOf<Map<Int, String>>()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(ByteArrayInputStream(bytes), "UTF-8")

        var currentRow: MutableMap<Int, String>? = null
        var currentCol = -1
        var cellType: String? = null
        var inV = false
        var inT = false
        val textBuf = StringBuilder()
        var fallbackCol = 0

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (localName(parser.name)) {
                        "row" -> {
                            currentRow = mutableMapOf()
                            fallbackCol = 0
                        }
                        "c" -> {
                            val ref = parser.getAttributeValue(null, "r")
                            currentCol = if (!ref.isNullOrBlank()) colIndexFromRef(ref) else fallbackCol
                            fallbackCol = currentCol + 1
                            cellType = parser.getAttributeValue(null, "t")
                            textBuf.clear()
                        }
                        "v" -> {
                            inV = true
                            textBuf.clear()
                        }
                        "t" -> {
                            inT = true
                            textBuf.clear()
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inV || inT) textBuf.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (localName(parser.name)) {
                        "v" -> inV = false
                        "t" -> inT = false
                        "c" -> {
                            val raw = textBuf.toString()
                            val resolved = when (cellType) {
                                "s" -> raw.toIntOrNull()?.let { sharedStrings.getOrNull(it) } ?: ""
                                else -> raw
                            }
                            if (currentCol >= 0) {
                                currentRow?.put(currentCol, resolved)
                            }
                            cellType = null
                        }
                        "row" -> {
                            currentRow?.let { if (it.isNotEmpty()) rows.add(it) }
                            currentRow = null
                        }
                    }
                }
            }
            event = parser.next()
        }

        if (rows.isEmpty()) return emptyList()
        val headers = rows.first()
        return rows.drop(1).map { row ->
            val map = mutableMapOf<String, String>()
            headers.forEach { (col, headerName) ->
                map[headerName] = row[col] ?: ""
            }
            map
        }
    }

    private fun colIndexFromRef(ref: String): Int {
        var idx = 0
        for (ch in ref) {
            if (ch in 'A'..'Z') {
                idx = idx * 26 + (ch - 'A' + 1)
            } else if (ch in 'a'..'z') {
                idx = idx * 26 + (ch - 'a' + 1)
            } else {
                break
            }
        }
        return (idx - 1).coerceAtLeast(0)
    }

    private fun localName(raw: String): String = raw.substringAfter(':')
}
