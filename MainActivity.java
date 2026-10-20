package com.example.classiccalculator;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import java.math.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class MainActivity extends Activity {
    CalcView calc;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().getDecorView().setSystemUiVisibility(0);
        calc = new CalcView();
        if (b != null) calc.engine.restore(b);
        setContentView(calc);
    }
    @Override public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b); calc.engine.save(b);
    }

    static class Engine {
        String text="0", op="", repeatOp="";
        BigDecimal stored=null, repeat=null;
        boolean fresh=true, error=false;
        static final MathContext MC=new MathContext(16, RoundingMode.HALF_EVEN);
        BigDecimal value() { return new BigDecimal(text); }
        void show(BigDecimal n) {
            text=n.round(MC).stripTrailingZeros().toPlainString(); error=false;
        }
        BigDecimal apply(BigDecimal a, String o, BigDecimal b) {
            switch(o) {
                case "+": return a.add(b,MC);
                case "−": return a.subtract(b,MC);
                case "×": return a.multiply(b,MC);
                case "÷": return a.divide(b,MC);
            }
            return b;
        }
        void clear() { text="0"; op=""; repeatOp=""; stored=null; repeat=null; fresh=true; error=false; }
        void press(String k) {
            try {
                if(k.equals("AC")) { clear(); return; }
                if(k.equals("C")) { text="0"; error=false; fresh=true; return; }
                if(error) clear();
                if(k.matches("[0-9]")) {
                    if(fresh) { text=k; fresh=false; }
                    else if(text.replace("-", "").replace(".", "").length()<12)
                        text=text.equals("0") ? k : text+k;
                    return;
                }
                if(k.equals(".")) {
                    if(fresh) { text="0."; fresh=false; }
                    else if(!text.contains(".")) text+=".";
                    return;
                }
                if(k.equals("±")) {
                    if(value().signum()!=0) text=text.startsWith("-")?text.substring(1):"-"+text;
                    return;
                }
                if(k.equals("%")) {
                    BigDecimal n=value().divide(new BigDecimal("100"),MC);
                    if(stored!=null && (op.equals("+")||op.equals("−"))) n=stored.multiply(n,MC);
                    show(n); fresh=false; return;
                }
                if(k.equals("=")) {
                    if(stored!=null && !op.isEmpty()) {
                        repeat=value(); repeatOp=op; show(apply(stored,op,repeat)); stored=null; op="";
                    } else if(repeat!=null) show(apply(value(),repeatOp,repeat));
                    fresh=true; return;
                }
                if(stored!=null && !fresh) show(apply(stored,op,value()));
                stored=value(); op=k; fresh=true; repeat=null; repeatOp="";
            } catch(ArithmeticException | NumberFormatException ex) {
                clear(); text="Error"; error=true;
            }
        }
        String display() {
            if(error) return "Error";
            BigDecimal n=value();
            if(n.abs().compareTo(new BigDecimal("999999999999"))>0 || (n.signum()!=0 && n.abs().compareTo(new BigDecimal("0.00000001"))<0)) {
                DecimalFormat f=new DecimalFormat("0.########E0", DecimalFormatSymbols.getInstance(Locale.US));
                return f.format(n);
            }
            return text;
        }
        void save(Bundle b) {
            b.putString("text",text); b.putString("op",op); b.putString("repeatOp",repeatOp);
            b.putString("stored",stored==null?null:stored.toString()); b.putString("repeat",repeat==null?null:repeat.toString());
            b.putBoolean("fresh",fresh); b.putBoolean("error",error);
        }
        void restore(Bundle b) {
            text=b.getString("text","0"); op=b.getString("op",""); repeatOp=b.getString("repeatOp","");
            stored=b.getString("stored")==null?null:new BigDecimal(b.getString("stored"));
            repeat=b.getString("repeat")==null?null:new BigDecimal(b.getString("repeat"));
            fresh=b.getBoolean("fresh",true); error=b.getBoolean("error");
        }
    }

    class CalcView extends View {
        final Engine engine=new Engine();
        final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        final ArrayList<Key> keys=new ArrayList<>();
        final String[][] rows={{"AC","±","%","÷"},{"7","8","9","×"},{"4","5","6","−"},{"1","2","3","+"},{"0",".","="}};
        String pressed="";
        final Runnable clearAll=()->{ if(pressed.equals("AC")){engine.clear();pressed="";invalidate();} };
        float unit, gap, left, top;
        class Key { String label; RectF rect; Key(String s, RectF r){label=s;rect=r;} }
        CalcView(){ super(MainActivity.this); setBackgroundColor(Color.BLACK); setFocusable(true); }
        float dp(float x){ return x*getResources().getDisplayMetrics().density; }
        @Override protected void onSizeChanged(int w,int h,int ow,int oh) {
            gap=dp(12); float width=Math.min(w-dp(24),dp(440));
            unit=Math.min((width-3*gap)/4,(h-dp(110)-4*gap)/5);
            left=(w-(4*unit+3*gap))/2; top=h-dp(18)-(5*unit+4*gap);
            keys.clear();
            for(int r=0;r<5;r++) for(int c=0;c<rows[r].length;c++) {
                int column=(r==4 && c>0)?c+1:c;
                float x=left+column*(unit+gap), y=top+r*(unit+gap);
                float widthKey=(r==4 && c==0)?2*unit+gap:unit;
                keys.add(new Key(rows[r][c],new RectF(x,y,x+widthKey,y+unit)));
            }
        }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            paint.setTypeface(Typeface.create("sans-serif-light",Typeface.NORMAL));
            paint.setColor(Color.WHITE); paint.setTextAlign(Paint.Align.RIGHT);
            String value=engine.display(); float size=dp(88); paint.setTextSize(size);
            float available=4*unit+3*gap;
            if(paint.measureText(value)>available) paint.setTextSize(size*available/paint.measureText(value));
            float baseline=top-dp(28);
            canvas.drawText(value,left+available-dp(5),baseline,paint);
            for(Key key:keys) {
                String k=key.label;
                boolean operator=k.equals("÷")||k.equals("×")||k.equals("−")||k.equals("+")||k.equals("=");
                boolean utility=k.equals("AC")||k.equals("±")||k.equals("%");
                boolean selected=operator && k.equals(engine.op) && engine.fresh;
                int color=operator?Color.rgb(255,159,10):utility?Color.rgb(165,165,165):Color.rgb(51,51,51);
                if(selected) color=Color.WHITE;
                if(k.equals(pressed)) color=operator?Color.rgb(255,198,110):utility?Color.rgb(215,215,215):Color.rgb(115,115,115);
                paint.setColor(color);
                canvas.drawRoundRect(key.rect,unit/2,unit/2,paint);
                paint.setColor(selected?Color.rgb(255,159,10):utility?Color.BLACK:Color.WHITE);
                paint.setTypeface(Typeface.create("sans-serif-light",Typeface.NORMAL));
                paint.setTextSize(unit*(utility?.40f:.49f));
                paint.setTextAlign(k.equals("0")?Paint.Align.LEFT:Paint.Align.CENTER);
                String label=k.equals("AC") && (!engine.text.equals("0")||!engine.op.isEmpty())?"C":k;
                float x=k.equals("0")?key.rect.left+unit*.36f:key.rect.centerX();
                float y=key.rect.centerY()-(paint.ascent()+paint.descent())/2;
                canvas.drawText(label,x,y,paint);
            }
        }
        Key at(float x,float y){ for(Key k:keys) if(k.rect.contains(x,y)) return k; return null; }
        @Override public boolean onTouchEvent(android.view.MotionEvent e) {
            Key k=at(e.getX(),e.getY());
            switch(e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if(k==null)return false; pressed=k.label; if(pressed.equals("AC"))postDelayed(clearAll,600); invalidate(); return true;
                case MotionEvent.ACTION_MOVE:
                    if(k==null || !k.label.equals(pressed)){pressed="";invalidate();} return true;
                case MotionEvent.ACTION_UP:
                    removeCallbacks(clearAll);
                    if(k!=null && k.label.equals(pressed)) {
                        String input=k.label;
                        if(input.equals("AC") && (!engine.text.equals("0")||!engine.op.isEmpty())) input="C";
                        engine.press(input); setContentDescription("Calculator result: "+engine.display()); performClick();
                    }
                    pressed="";invalidate();return true;
                case MotionEvent.ACTION_CANCEL: removeCallbacks(clearAll); pressed="";invalidate();return true;
            }
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
