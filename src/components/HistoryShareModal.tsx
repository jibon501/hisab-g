import React, { useRef, useState, useEffect } from 'react';
import { HistoryItem, tk, fmtDate } from '../data/models';
import { X, Download, Share2, Copy, Check, Printer } from 'lucide-react';

interface HistoryShareModalProps {
  person: string;
  kind: 'loan' | 'lent' | 'shop';
  items: HistoryItem[];
  onClose: () => void;
}

export const HistoryShareModal: React.FC<HistoryShareModalProps> = ({
  person,
  kind,
  items,
  onClose,
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [copied, setCopied] = useState(false);
  const [imgUrl, setImgUrl] = useState<string | null>(null);

  const kindLabel =
    kind === 'lent'
      ? 'পাওনার হিসাব (ধার দেওয়া)'
      : kind === 'loan'
      ? 'দেনার হিসাব (লোন গ্রহণ)'
      : 'দোকান বাকি হিসাব';

  const remaining = items.reduce((acc, h) => acc + (h.payment ? -h.amount : h.amount), 0);

  // Render on canvas
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const width = 720;
    const pad = 40;
    const rowH = 68;
    const headH = 160;
    const footH = 120;
    const height = Math.max(340, Math.floor(headH + rowH * items.length + footH));

    canvas.width = width;
    canvas.height = height;

    // Background
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, width, height);

    // Header top decorative bar
    ctx.fillStyle = kind === 'lent' ? '#16a34a' : kind === 'loan' ? '#dc2626' : '#9333ea';
    ctx.fillRect(0, 0, width, 8);

    // Title & kind
    ctx.fillStyle = '#0f172a';
    ctx.font = 'bold 36px "Hind Siliguri", sans-serif';
    ctx.fillText(person, pad, 64);

    ctx.fillStyle = '#64748b';
    ctx.font = '500 20px "Hind Siliguri", sans-serif';
    ctx.fillText(`${kindLabel} • হিসাবের স্লিপ`, pad, 102);

    ctx.fillStyle = '#94a3b8';
    ctx.font = '16px "Hind Siliguri", sans-serif';
    ctx.fillText(`মুদ্রণ তারিখ: ${fmtDate(new Date().toISOString())}`, pad, 130);

    // Header divider line
    ctx.strokeStyle = '#e2e8f0';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(pad, headH - 10);
    ctx.lineTo(width - pad, headH - 10);
    ctx.stroke();

    // Table rows
    let y = headH + 34;

    items.forEach((h) => {
      // Date
      ctx.fillStyle = '#64748b';
      ctx.font = '500 18px "Hind Siliguri", sans-serif';
      ctx.fillText(fmtDate(h.date), pad, y);

      // Label
      ctx.fillStyle = '#1e293b';
      ctx.font = '500 20px "Hind Siliguri", sans-serif';
      ctx.fillText(h.label, pad + 150, y);

      // Amount
      const amtStr = (h.payment ? '- ' : '+ ') + tk(h.amount);
      ctx.fillStyle = h.payment ? '#16a34a' : '#b91c1c';
      ctx.font = 'bold 22px "Hind Siliguri", sans-serif';
      const textWidth = ctx.measureText(amtStr).width;
      ctx.fillText(amtStr, width - pad - textWidth, y);

      y += rowH;
    });

    // Footer divider line
    ctx.strokeStyle = '#cbd5e1';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(pad, y - rowH + 30);
    ctx.lineTo(width - pad, y - rowH + 30);
    ctx.stroke();

    // Net remaining
    const restText = `মোট বাকি: ${tk(remaining)}`;
    ctx.fillStyle = remaining > 0 ? '#b91c1c' : '#16a34a';
    ctx.font = 'bold 30px "Hind Siliguri", sans-serif';
    const restWidth = ctx.measureText(restText).width;
    ctx.fillText(restText, width - pad - restWidth, y + 26);

    // Footer signature / watermark
    ctx.fillStyle = '#94a3b8';
    ctx.font = '16px "Hind Siliguri", sans-serif';
    ctx.fillText('হিসাব (HISAB) অ্যাপ দ্বারা প্রস্তুতকৃত', pad, y + 26);

    try {
      setImgUrl(canvas.toDataURL('image/png'));
    } catch {
      // ignore
    }
  }, [person, kind, items, kindLabel, remaining]);

  const handleDownload = () => {
    if (!imgUrl) return;
    const a = document.createElement('a');
    a.href = imgUrl;
    a.download = `হিসাব-${person}-${new Date().toISOString().slice(0, 10)}.png`;
    a.click();
  };

  const handleCopyText = () => {
    let text = `${person} • ${kindLabel}\n`;
    text += `তারিখ: ${fmtDate(new Date().toISOString())}\n`;
    text += '------------------------------------\n';
    items.forEach((h) => {
      text += `${fmtDate(h.date)}  ${h.label}  ${h.payment ? 'পরিশোধ:' : ''} ${tk(h.amount)}\n`;
    });
    text += '------------------------------------\n';
    text += `মোট বাকি: ${tk(remaining)}\n`;

    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleShare = async () => {
    if (navigator.share) {
      try {
        if (imgUrl) {
          const res = await fetch(imgUrl);
          const blob = await res.blob();
          const file = new File([blob], `hisab-${person}.png`, { type: 'image/png' });
          if (navigator.canShare && navigator.canShare({ files: [file] })) {
            await navigator.share({
              files: [file],
              title: `${person} - হিসাব বিবরণী`,
              text: `${person}-এর হিসাব বিবরণী। বাকি: ${tk(remaining)}`,
            });
            return;
          }
        }
        await navigator.share({
          title: `${person} - হিসাব বিবরণী`,
          text: `${person} (${kindLabel})\nমোট বাকি: ${tk(remaining)}`,
        });
      } catch {
        handleCopyText();
      }
    } else {
      handleCopyText();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl shadow-2xl max-w-lg w-full max-h-[90vh] flex flex-col overflow-hidden">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between bg-slate-50">
          <div>
            <h3 className="font-bold text-slate-800 text-lg">{person}</h3>
            <p className="text-xs text-slate-500">{kindLabel} বিবরণী</p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-full hover:bg-slate-200 text-slate-500 transition-colors"
          >
            <X size={20} />
          </button>
        </div>

        {/* Content Preview */}
        <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-slate-100/70">
          {/* Visual Receipt Canvas */}
          <div className="bg-white p-2 rounded-xl shadow-xs border border-slate-200 overflow-x-auto flex justify-center">
            <canvas ref={canvasRef} className="max-w-full h-auto rounded-lg shadow-xs" />
          </div>

          {/* Text summary fallback */}
          <div className="bg-white p-3 rounded-xl border border-slate-200 text-xs text-slate-600 space-y-1">
            <div className="flex justify-between font-semibold text-slate-700">
              <span>মোট লেনদেন সংখ্যা:</span>
              <span>{items.length} টি</span>
            </div>
            <div className="flex justify-between font-bold text-slate-900 text-sm pt-1 border-t border-slate-100">
              <span>অবশিষ্ট বাকি:</span>
              <span className={remaining > 0 ? 'text-red-600' : 'text-emerald-600'}>
                {tk(remaining)}
              </span>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="p-4 border-t border-slate-200 bg-white grid grid-cols-3 gap-2">
          <button
            onClick={handleDownload}
            className="flex items-center justify-center gap-1.5 px-3 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-medium text-xs sm:text-sm shadow-xs transition-colors"
          >
            <Download size={16} />
            <span>ছবি সেভ</span>
          </button>
          <button
            onClick={handleShare}
            className="flex items-center justify-center gap-1.5 px-3 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-medium text-xs sm:text-sm shadow-xs transition-colors"
          >
            <Share2 size={16} />
            <span>শেয়ার</span>
          </button>
          <button
            onClick={handleCopyText}
            className="flex items-center justify-center gap-1.5 px-3 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium text-xs sm:text-sm transition-colors"
          >
            {copied ? <Check size={16} className="text-emerald-600" /> : <Copy size={16} />}
            <span>{copied ? 'কপি হয়েছে' : 'টেক্সট কপি'}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
