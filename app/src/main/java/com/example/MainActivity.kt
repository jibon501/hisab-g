package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.CategoryKind
import com.example.data.model.DebtKind
import com.example.data.model.tk
import com.example.ui.HisabViewModel
import com.example.ui.components.AddTxnDialog
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DebtsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.HisabTheme
import com.example.ui.theme.Red500
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import kotlinx.serialization.Serializable
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Serializable
sealed interface HisabRoute {
    @Serializable
    data object Dashboard : HisabRoute

    @Serializable
    data object Transactions : HisabRoute

    @Serializable
    data object Lent : HisabRoute

    @Serializable
    data object Loan : HisabRoute

    @Serializable
    data object Accounts : HisabRoute
}

private data class NavTab(
    val route: HisabRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HisabTheme {
                HisabApp()
            }
        }
    }
}

@Composable
fun HisabApp(viewModel: HisabViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snapshot = uiState.snapshot

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination

    var showGlobalAddTxn by remember { mutableStateOf(false) }

    val tabs = remember {
        listOf(
            NavTab(
                route = HisabRoute.Dashboard,
                label = "ড্যাশবোর্ড",
                selectedIcon = Icons.Filled.Dashboard,
                unselectedIcon = Icons.Outlined.Dashboard,
                testTag = "nav_tab_dashboard"
            ),
            NavTab(
                route = HisabRoute.Transactions,
                label = "লেনদেন",
                selectedIcon = Icons.Filled.Receipt,
                unselectedIcon = Icons.Outlined.Receipt,
                testTag = "nav_tab_txns"
            ),
            NavTab(
                route = HisabRoute.Lent,
                label = "ধার দেওয়া",
                selectedIcon = Icons.AutoMirrored.Filled.TrendingUp,
                unselectedIcon = Icons.AutoMirrored.Filled.TrendingUp,
                testTag = "nav_tab_lent"
            ),
            NavTab(
                route = HisabRoute.Loan,
                label = "ধার নেওয়া",
                selectedIcon = Icons.AutoMirrored.Filled.TrendingDown,
                unselectedIcon = Icons.AutoMirrored.Filled.TrendingDown,
                testTag = "nav_tab_loan"
            ),
            NavTab(
                route = HisabRoute.Accounts,
                label = "অ্যাকাউন্ট",
                selectedIcon = Icons.Filled.AccountBalanceWallet,
                unselectedIcon = Icons.Outlined.AccountBalanceWallet,
                testTag = "nav_tab_accounts"
            )
        )
    }

    fun navigateToTab(route: HisabRoute) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Slate50)) {
        val useRail = maxWidth >= 680.dp

        Scaffold(
            containerColor = Slate50,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                HisabTopBar(
                    cashBalance = tk(snapshot.summary.cash),
                    compactTitle = maxWidth < 400.dp,
                    onOpenAddTxn = { showGlobalAddTxn = true }
                )
            },
            bottomBar = {
                if (!useRail) {
                    Surface(
                        color = Color.White.copy(alpha = 0.96f),
                        shadowElevation = 6.dp
                    ) {
                        Column(modifier = Modifier.navigationBarsPadding()) {
                            HorizontalDivider(color = Slate200)
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                windowInsets = WindowInsets(0, 0, 0, 0)
                            ) {
                                tabs.forEach { tab ->
                                    val selected = currentDest?.hasRoute(tab.route::class) == true
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateToTab(tab.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.label,
                                                fontSize = 10.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Emerald700,
                                            selectedTextColor = Emerald700,
                                            indicatorColor = Emerald50,
                                            unselectedIconColor = Slate500,
                                            unselectedTextColor = Slate500
                                        ),
                                        modifier = Modifier.testTag(tab.testTag)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = Color.White,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        Spacer(Modifier.size(8.dp))
                        tabs.forEach { tab ->
                            val selected = currentDest?.hasRoute(tab.route::class) == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label, fontSize = 11.sp) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Emerald700,
                                    selectedTextColor = Emerald700,
                                    indicatorColor = Emerald50,
                                    unselectedIconColor = Slate500,
                                    unselectedTextColor = Slate500
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = HisabRoute.Dashboard,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 860.dp)
                    ) {
                        composable<HisabRoute.Dashboard> {
                            DashboardScreen(
                                snapshot = snapshot,
                                getDebtHistory = viewModel::getDebtHistory,
                                onPayDebt = viewModel::payDebt
                            )
                        }
                        composable<HisabRoute.Transactions> {
                            TransactionsScreen(
                                snapshot = snapshot,
                                txns = uiState.filteredTxns,
                                filter = uiState.categoryFilter,
                                onApplyFilter = viewModel::applyFilter,
                                onAddTxns = viewModel::addTxns,
                                onAddBakiExpense = viewModel::addBakiExpense,
                                onDeleteTxn = viewModel::deleteTxn,
                                onTransfer = viewModel::transfer,
                                onAddCategory = viewModel::addCategory,
                                onRenameCategory = viewModel::renameCategory,
                                onDeleteCategory = viewModel::deleteCategory
                            )
                        }
                        composable<HisabRoute.Lent> {
                            DebtsScreen(
                                kind = DebtKind.LENT,
                                snapshot = snapshot,
                                getDebtHistory = viewModel::getDebtHistory,
                                onAddDebt = viewModel::addDebt,
                                onSaveDebtEdit = viewModel::saveDebtEdit,
                                onDeleteDebt = viewModel::deleteDebt
                            )
                        }
                        composable<HisabRoute.Loan> {
                            DebtsScreen(
                                kind = DebtKind.LOAN,
                                snapshot = snapshot,
                                getDebtHistory = viewModel::getDebtHistory,
                                onAddDebt = viewModel::addDebt,
                                onSaveDebtEdit = viewModel::saveDebtEdit,
                                onDeleteDebt = viewModel::deleteDebt
                            )
                        }
                        composable<HisabRoute.Accounts> {
                            AccountsScreen(
                                snapshot = snapshot,
                                statusMessage = uiState.statusMessage,
                                errorMessage = uiState.errorMessage,
                                onClearMessages = viewModel::clearMessages,
                                onAddAccount = viewModel::addAccount,
                                onUpdateAccount = viewModel::updateAccount,
                                onDeleteAccount = viewModel::deleteAccount,
                                onExportXlsx = viewModel::exportXlsxToUri,
                                onImportXlsx = viewModel::importXlsxFromUri,
                                onResetToDefault = viewModel::resetToDefault
                            )
                        }
                    }
                }
            }
        }

        if (uiState.celebrateTick > 0) {
            ConfettiCelebrationOverlay(triggerKey = uiState.celebrateTick)
        }
    }

    if (showGlobalAddTxn) {
        val expenseCats = remember(snapshot.categories) {
            snapshot.categories.filter { it.kind == CategoryKind.EXPENSE }.map { it.name }
        }
        val incomeCats = remember(snapshot.categories) {
            snapshot.categories.filter { it.kind == CategoryKind.INCOME }.map { it.name }
        }
        AddTxnDialog(
            accounts = snapshot.accounts,
            expenseCats = expenseCats,
            incomeCats = incomeCats,
            onClose = { showGlobalAddTxn = false },
            onSave = { type, splits, cat, note, baki, date ->
                if (baki) {
                    viewModel.addBakiExpense(splits.sumOf { it.amount }, cat, note, date)
                } else {
                    viewModel.addTxns(type, splits, cat, note, date)
                }
                showGlobalAddTxn = false
            }
        )
    }
}

@Composable
private fun HisabTopBar(
    cashBalance: String,
    compactTitle: Boolean,
    onOpenAddTxn: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Emerald600),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "হি",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "হিসাব",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Slate900
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Emerald100
                            ) {
                                Text(
                                    text = "HISAB",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald800,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                        if (!compactTitle) {
                            Text(
                                text = "ব্যক্তিগত আয়, ব্যয় ও দেনা-পাওনা",
                                fontSize = 10.sp,
                                color = Slate500
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!compactTitle) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ক্যাশ ব্যালেন্স", fontSize = 10.sp, color = Slate400)
                            Text(cashBalance, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald700)
                        }
                    }

                    Button(
                        onClick = onOpenAddTxn,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("global_add_txn_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (compactTitle) "যোগ" else "নতুন লেনদেন",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            HorizontalDivider(color = Slate200.copy(alpha = 0.8f))
        }
    }
}

private data class ConfettiParticle(
    val angle: Float,
    val speed: Float,
    val radius: Float,
    val color: Color
)

@Composable
private fun ConfettiCelebrationOverlay(triggerKey: Int) {
    val progress = remember(triggerKey) { Animatable(0f) }
    val particles = remember(triggerKey) {
        val colors = listOf(Emerald600, Amber500, Blue600, Red500, Color(0xFF9333EA))
        List(45) { i ->
            ConfettiParticle(
                angle = Random(triggerKey * 100 + i).nextFloat() * 6.283f,
                speed = 160f + Random(triggerKey * 200 + i).nextFloat() * 340f,
                radius = 6f + Random(triggerKey * 300 + i).nextFloat() * 6f,
                color = colors[i % colors.size]
            )
        }
    }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 1200))
    }

    if (progress.value < 1f) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val origin = Offset(size.width / 2f, size.height * 0.65f)
            val t = progress.value
            val alpha = (1f - t).coerceIn(0f, 1f)
            particles.forEach { p ->
                val dx = cos(p.angle) * p.speed * t
                val dy = sin(p.angle) * p.speed * t + 320f * t * t
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.radius,
                    center = Offset(origin.x + dx, origin.y + dy)
                )
            }
        }
    }
}
