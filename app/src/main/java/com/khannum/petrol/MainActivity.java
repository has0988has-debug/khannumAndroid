package com.khannum.petrol;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final int GREEN = Color.rgb(0, 105, 58);
    private static final String PREFS = "khannum_data";
    private static final String LAST_DATA = "last_data";
    private static final String HISTORY = "history";
    private static final int ROWS = 10;

    private InvoiceCanvas canvas;
    private Bitmap template;
    private EditText invoiceNo, customer, car, address, date, discount;
    private final EditText[] types = new EditText[ROWS];
    private final EditText[] qty = new EditText[ROWS];
    private final EditText[] price = new EditText[ROWS];
    private final TextView[] lineTotal = new TextView[ROWS];
    private TextView totalAll, net;
    private android.content.SharedPreferences prefs;
    private int invoiceNumber;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        template = loadAsset();
        buildUi();
        newInvoice();
    }

    private Bitmap loadAsset() {
        try { return BitmapFactory.decodeStream(getAssets().open("invoice_template.jpg")); }
        catch (Exception e) { return null; }
    }

    private EditText edit(int size) {
        EditText e = new EditText(this);
        e.setTextSize(size);
        e.setTextColor(GREEN);
        e.setHintTextColor(Color.TRANSPARENT);
        e.setGravity(Gravity.CENTER);
        e.setSingleLine(true);
        e.setPadding(3, 0, 3, 0);
        e.setBackground(new ColorDrawable(Color.TRANSPARENT));
        e.setTextDirection(View.TEXT_DIRECTION_RTL);
        e.setSelectAllOnFocus(false);
        e.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) { calculate(); }
            public void afterTextChanged(android.text.Editable e) {}
        });
        return e;
    }

    private TextView totalText() {
        TextView t = new TextView(this);
        t.setTextColor(GREEN);
        t.setTextSize(15);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setPadding(4, 4, 4, 4);
        toolbar.setBackgroundColor(Color.WHITE);
        addButton(toolbar, "فاتورة جديدة", v -> newInvoice());
        addButton(toolbar, "حفظ", v -> saveInvoice());
        addButton(toolbar, "الفواتير", v -> showHistory());
        addButton(toolbar, "PDF", v -> createPdf(true));
        root.addView(toolbar, new LinearLayout.LayoutParams(-1, dp(54)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        canvas = new InvoiceCanvas(this);
        scroll.addView(canvas, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void addButton(LinearLayout bar, String label, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(12);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setBackgroundColor(GREEN);
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -1, 1);
        p.setMargins(2, 0, 2, 0);
        bar.addView(b, p);
    }

    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + .5f); }

    private void newInvoice() {
        invoiceNumber = prefs.getInt("last_number", 0) + 1;
        invoiceNo.setText(String.format(Locale.US, "%06d", invoiceNumber));
        customer.setText(""); car.setText(""); address.setText("");
        date.setText(new SimpleDateFormat("yyyy/MM/dd", Locale.US).format(new Date()));
        discount.setText("0");
        for (int i = 0; i < ROWS; i++) { types[i].setText(""); qty[i].setText(""); price[i].setText(""); }
        calculate();
        canvas.scrollTo(0, 0);
    }

    private double number(String value) {
        if (value == null) return 0;
String s = value.trim().replace(",", ".");        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    private String money(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) return String.format(Locale.US, "%.0f", value);
        return String.format(Locale.US, "%.2f", value);
    }

    private void calculate() {
        double sum = 0;
        for (int i = 0; i < ROWS; i++) {
            double v = number(qty[i].getText().toString()) * number(price[i].getText().toString());
            sum += v;
            lineTotal[i].setText(v == 0 ? "" : money(v));
        }
        double d = number(discount.getText().toString());
        double finalValue = Math.max(0, sum - d);
        totalAll.setText(money(sum));
        net.setText(money(finalValue));
    }

    private String pack() {
        StringBuilder s = new StringBuilder();
        String[] all = new String[6 + ROWS * 3];
        all[0] = invoiceNo.getText().toString(); all[1] = customer.getText().toString();
        all[2] = car.getText().toString(); all[3] = address.getText().toString();
        all[4] = date.getText().toString(); all[5] = discount.getText().toString();
        int k = 6;
        for (int i = 0; i < ROWS; i++) { all[k++] = types[i].getText().toString(); all[k++] = qty[i].getText().toString(); all[k++] = price[i].getText().toString(); }
        for (String x : all) s.append(encode(x)).append("|");
        return s.toString();
    }

    private String encode(String s) { return s.replace("\\", "\\\\").replace("|", "\\p"); }
    private String decode(String s) { return s.replace("\\p", "|").replace("\\\\", "\\"); }

    private void unpack(String data) {
        String[] a = data.split("\\|", -1);
        ArrayList<String> values = new ArrayList<>();
        for (String x : a) if (!x.isEmpty()) values.add(decode(x));
        if (values.size() < 6) return;
        invoiceNo.setText(values.get(0)); customer.setText(values.get(1)); car.setText(values.get(2));
        address.setText(values.get(3)); date.setText(values.get(4)); discount.setText(values.get(5));
        int k = 6;
        for (int i = 0; i < ROWS && k + 2 < values.size(); i++) { types[i].setText(values.get(k++)); qty[i].setText(values.get(k++)); price[i].setText(values.get(k++)); }
        try { invoiceNumber = Integer.parseInt(invoiceNo.getText().toString().replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
        calculate();
    }

    private void saveInvoice() {
        prefs.edit().putInt("last_number", Math.max(prefs.getInt("last_number", 0), invoiceNumber)).putString(LAST_DATA, pack()).apply();
        String item = invoiceNumber + "~" + customer.getText().toString().replace("~", " ") + "~" + date.getText().toString() + "~" + pack();
        String history = prefs.getString(HISTORY, "");
        ArrayList<String> list = new ArrayList<>();
        if (!history.isEmpty()) list.addAll(Arrays.asList(history.split("\\n", -1)));
        for (int i = list.size() - 1; i >= 0; i--) if (list.get(i).startsWith(invoiceNumber + "~")) list.remove(i);
        list.add(0, item);
        while (list.size() > 50) list.remove(list.size() - 1);
        prefs.edit().putString(HISTORY, join(list, "\n")).apply();
        Toast.makeText(this, "تم حفظ الفاتورة رقم " + invoiceNo.getText(), Toast.LENGTH_SHORT).show();
    }

    private String join(List<String> list, String sep) { StringBuilder b = new StringBuilder(); for (String x : list) { if (b.length() > 0) b.append(sep); b.append(x); } return b.toString(); }

    private void showHistory() {
        String history = prefs.getString(HISTORY, "");
        if (history.isEmpty()) { Toast.makeText(this, "لا توجد فواتير محفوظة", Toast.LENGTH_SHORT).show(); return; }
        ArrayList<String> records = new ArrayList<>(Arrays.asList(history.split("\\n")));
        String[] labels = new String[records.size()];
        for (int i = 0; i < records.size(); i++) {
            String[] p = records.get(i).split("~", 4);
            labels[i] = "فاتورة " + p[0] + "  •  " + (p.length > 1 ? p[1] : "") + "  •  " + (p.length > 2 ? p[2] : "");
        }
        new AlertDialog.Builder(this).setTitle("الفواتير المحفوظة").setItems(labels, (d, which) -> {
            String[] p = records.get(which).split("~", 4);
            if (p.length == 4) unpack(p[3]);
        }).setNegativeButton("إلغاء", null).show();
    }

    private void createPdf(boolean share) {
        calculate();
        try {
            android.graphics.pdf.PdfDocument doc = new android.graphics.pdf.PdfDocument();
            android.graphics.pdf.PdfDocument.Page page = doc.startPage(new android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create());
            Canvas c = page.getCanvas();
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            c.drawBitmap(template, null, new Rect(0, 0, 595, 842), paint);
            paint.setColor(GREEN); paint.setTextAlign(Paint.Align.CENTER); paint.setTextSize(9);
            float sx = 595f / template.getWidth(), sy = 842f / template.getHeight();
            draw(c, paint, invoiceNo, 177, 378, sx, sy);
            draw(c, paint, customer, 695, 378, sx, sy);
            draw(c, paint, date, 177, 430, sx, sy);
            draw(c, paint, car, 695, 430, sx, sy);
            draw(c, paint, address, 695, 482, sx, sy);
            for (int i = 0; i < ROWS; i++) {
                float y = 620 + i * 47;
                draw(c, paint, types[i], 800, y, sx, sy);
                draw(c, paint, qty[i], 585, y, sx, sy);
                draw(c, paint, price[i], 400, y, sx, sy);
                drawText(c, paint, lineTotal[i].getText().toString(), 150, y, sx, sy);
            }
            drawText(c, paint, totalAll.getText().toString(), 720, 1095, sx, sy);
            drawText(c, paint, discount.getText().toString(), 720, 1145, sx, sy);
            drawText(c, paint, net.getText().toString(), 720, 1195, sx, sy);
            doc.finishPage(page);

            File file = new File(getExternalFilesDir(null), "فاتورة_" + invoiceNo.getText() + ".pdf");
            FileOutputStream out = new FileOutputStream(file); doc.writeTo(out); out.close(); doc.close();
            Uri uri = publishPdf(file);
            if (share && uri != null) {
                Intent send = new Intent(Intent.ACTION_SEND); send.setType("application/pdf"); send.putExtra(Intent.EXTRA_STREAM, uri); send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(send, "مشاركة الفاتورة PDF"));
            } else Toast.makeText(this, "تم إنشاء ملف PDF", Toast.LENGTH_LONG).show();
        } catch (Exception e) { Toast.makeText(this, "تعذر إنشاء PDF: " + e.getMessage(), Toast.LENGTH_LONG).show(); }
    }

    private void draw(Canvas c, Paint p, EditText e, float x, float y, float sx, float sy) { drawText(c, p, e.getText().toString(), x, y, sx, sy); }
    private void drawText(Canvas c, Paint p, String s, float x, float y, float sx, float sy) { if (s != null && !s.isEmpty()) c.drawText(s, x * sx, y * sy, p); }

    private Uri publishPdf(File file) throws Exception {
        if (Build.VERSION.SDK_INT >= 29) {
            ContentValues v = new ContentValues();
            v.put(MediaStore.Downloads.DISPLAY_NAME, file.getName());
            v.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            v.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Khannum");
            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
            if (uri != null) { try (InputStream in = new FileInputStream(file); OutputStream out = getContentResolver().openOutputStream(uri)) { byte[] buf = new byte[8192]; int n; while ((n = in.read(buf)) > 0) out.write(buf, 0, n); } return uri; }
        }
        return null;
    }

    private class InvoiceCanvas extends FrameLayout {
        private ImageView bg;
        InvoiceCanvas(Context c) { super(c); setWillNotDraw(false); bg = new ImageView(c); bg.setImageBitmap(template); bg.setScaleType(ImageView.ScaleType.FIT_XY); addView(bg); createFields(); }

        private void createFields() {
            invoiceNo = edit(17); customer = edit(17); car = edit(17); address = edit(17); date = edit(17); discount = edit(16);
            addView(invoiceNo); addView(customer); addView(date); addView(car); addView(address); addView(discount);
            for (int i = 0; i < ROWS; i++) { types[i] = edit(15); qty[i] = edit(15); price[i] = edit(15); lineTotal[i] = totalText(); addView(types[i]); addView(qty[i]); addView(price[i]); addView(lineTotal[i]); }
            totalAll = totalText(); net = totalText(); addView(totalAll); addView(net);
        }

        @Override protected void onMeasure(int widthSpec, int heightSpec) {
            int w = MeasureSpec.getSize(widthSpec);
            if (w <= 0) w = dp(360);
            int h = Math.round(w * template.getHeight() / (float) template.getWidth());
            setMeasuredDimension(w, h);
            int fieldH = Math.max(dp(32), h / 40);
            measureChild(bg, MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
            for (int i = 0; i < getChildCount(); i++) if (getChildAt(i) != bg) getChildAt(i).measure(MeasureSpec.makeMeasureSpec(Math.max(1, w / 4), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(fieldH, MeasureSpec.EXACTLY));
        }

        private void pos(View v, float l, float t, float r, float b, int w, int h) {
            v.layout(Math.round(l*w), Math.round(t*h), Math.round(r*w), Math.round(b*h));
        }

        @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int w = r-l, h = b-t;
            bg.layout(0,0,w,h);
            pos(invoiceNo,.045f,.229f,.305f,.263f,w,h); pos(customer,.485f,.229f,.955f,.263f,w,h);
            pos(date,.045f,.263f,.305f,.297f,w,h); pos(car,.485f,.263f,.955f,.297f,w,h); pos(address,.485f,.297f,.955f,.331f,w,h);
            pos(discount,.40f,.704f,.78f,.736f,w,h);
            for(int i=0;i<ROWS;i++){
                float y=.377f+i*.0306f;
                pos(types[i],.655f,y,.91f,y+.031f,w,h);
                pos(qty[i],.486f,y,.655f,y+.031f,w,h);
                pos(price[i],.288f,y,.486f,y+.031f,w,h);
                pos(lineTotal[i],.027f,y,.288f,y+.031f,w,h);
            }
            pos(totalAll,.40f,.688f,.78f,.716f,w,h); pos(net,.40f,.768f,.78f,.796f,w,h);
        }
    }
}
