package tabu;

import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import javax.microedition.rms.*;
import java.io.InputStream;
import java.util.Random;

public class GameCanvas extends Canvas implements Runnable {

    // Ekranlar
    private static final int S_MENU=0,S_AYAR=1,S_HAZIR=2;
    private static final int S_OYUN=3,S_FLASH=4,S_TUR_BIT=5,S_KAZANAN=6,S_ISTAT=7,S_TAKIM_AD=8;

    // Tuslar
    private static final int K0=48,K1=49,K2=50,K3=51,K4=52;
    private static final int K5=53,K6=54,K7=55,K8=56,K9=57;
    private static final int KSTAR=42,KHASH=35;

    // 8 Tema: {BG, CARD, DARK, BORDER, RED, GREEN, YELLOW, WHITE, GRAY, LGRAY, GOLD, ACCENT}
    private static final int[][] TM = {
        {0x0D1117,0x161B22,0x21262D,0x30363D,0xE94560,0x3FB950,0xD4A017,0xF0F6FC,0x8B949E,0x484F58,0xFFD700,0xFF79C6},
        {0x2D0018,0x4A0028,0x660038,0x880050,0xFF1493,0xFF69B4,0xFF8C00,0xFFE4F0,0xCC77AA,0x880040,0xFF1493,0xFF69B4},
        {0xFFF0F8,0xFFDAEA,0xFFC0D8,0xFFAACC,0xFF1493,0x27AE60,0xE67E22,0x2D0015,0x994466,0xDDAACB,0xFF1493,0xE91E8C},
        {0x020818,0x061530,0x0A2248,0x103060,0x4488FF,0x00FF88,0xFFDD00,0xDDEEFF,0x4466AA,0x112244,0x88AAFF,0x00DDFF},
        {0x180800,0x2D1400,0x3D1E00,0x5C2E00,0xFF6600,0x44FF88,0xFFEE00,0xFFF0E0,0xAA7744,0x663300,0xFFAA00,0xFF4400},
        {0x001200,0x001E00,0x002800,0x003C00,0x44FF44,0x00DDFF,0xFFEE00,0xEEFFEE,0x44AA44,0x002800,0x88FF88,0x00FF44},
        {0x12001A,0x1E002D,0x28003D,0x3C005C,0xFF44FF,0x44FFAA,0xFFEE00,0xF8EEFF,0xAA44BB,0x280038,0xFF88FF,0xFF00FF},
        {0x181800,0x2D2D00,0x3D3D00,0x5C5C00,0xFFFF00,0x00FFAA,0xFF6600,0xFFFFF0,0xAAAA44,0x333300,0xFFFF88,0xFFDD00},
    };
    private static final String[][] TM_AD={
        {"Koyu","Dark"},{"Pembe K.","Pink D."},{"Pembe A.","Pink L."},
        {"Mavi","Blue"},{"Turuncu","Orange"},{"Orman","Forest"},{"Mor","Purple"},{"Sari","Yellow"}
    };

    private static final int[] SUR={30,45,60,90,120};
    private static final int[] PAS={0,1,2,3,5,99};
    private static final int[] TUR={1,2,3,4,5,99};

    private TabuMIDlet midlet;
    private Thread thread;
    private boolean running=true;
    private int ekran=S_MENU;
    private int W,H;
    private int[] T; // aktif tema

    // Ses
    private Player muzikPlayer=null;
    private boolean sesAcik=true,titAcik=true,muzikAcik=true;

    // Bayraklar
    private Image imgTR=null,imgEN=null;

    // Ayarlar
    private int temaIdx=0,dil=0,ayarSec=0;
    private int takimS=2,sureIdx=2,turSuresi=60,pasIdx=2,turIdx=0;

    // Oyun
    private int[] puan,toplamPuan;
    private String[] takimAd;
    private int aktif=0,mTur=1;
    private int[] deste;
    private int dIdx=0;
    private int kSure,tDogru=0,tTabu=0,tPas=0,kPas=0;
    private long turStart,flashStart=0;
    private boolean turAktif=false;

    // Istatistik
    private int totD=0,totT=0,totP=0,enSkor=0;
    private String enTakim="---";
    private String[] gecmis=new String[5];

    // Kazanan
    private int kazIdx=0;

    // Kart animasyonu
    private long animT=0;
    private int animYon=0,kartOff=0;

    // Menu
    private int menuSec=0;
    private int adTakim=0,adSec=0;

    public GameCanvas(TabuMIDlet m) {
        midlet=m;
        setFullScreenMode(true);
        W=getWidth();H=getHeight();
        takimAd=new String[]{"Takim 1","Takim 2","Takim 3","Takim 4","Takim 5","Takim 6"};
        puan=new int[6];toplamPuan=new int[6];
        for(int i=0;i<5;i++)gecmis[i]="";
        yukle();
        T=TM[temaIdx];
        resimYukle();
        muzikBaslat();
        thread=new Thread(this);
        thread.start();
    }

    // ── Resim yükleme ─────────────────────────────────────────────────────────
    private void resimYukle(){
        try{InputStream is=getClass().getResourceAsStream("/tr.png");if(is!=null)imgTR=Image.createImage(is);}catch(Exception e){}
        try{InputStream is=getClass().getResourceAsStream("/en.png");if(is!=null)imgEN=Image.createImage(is);}catch(Exception e){}
    }

    // ── Müzik ────────────────────────────────────────────────────────────────
    private void muzikBaslat(){
        if(!muzikAcik)return;
        try{
            if(muzikPlayer!=null){muzikPlayer.stop();muzikPlayer.close();muzikPlayer=null;}
            InputStream is=getClass().getResourceAsStream("/music.mid");
            if(is==null)return;
            muzikPlayer=Manager.createPlayer(is,"audio/midi");
            muzikPlayer.setLoopCount(-1);
            muzikPlayer.realize();
            try{
                VolumeControl vc=(VolumeControl)muzikPlayer.getControl("VolumeControl");
                if(vc!=null)vc.setLevel(60);
            }catch(Exception e){}
            muzikPlayer.start();
        }catch(Exception e){muzikPlayer=null;}
    }

    private void muzikDur(){
        try{if(muzikPlayer!=null){muzikPlayer.stop();muzikPlayer.close();muzikPlayer=null;}}catch(Exception e){}
    }

    // ── Ses/Titreşim ─────────────────────────────────────────────────────────
    private void bipSes(){
        if(!sesAcik)return;
        try{midlet.getDisplay().flashBacklight(150);}catch(Exception e){}
    }

    private void titret(){
        if(!titAcik)return;
        try{midlet.getDisplay().vibrate(300);}catch(Exception e){}
    }

    // ── RecordStore ───────────────────────────────────────────────────────────
    private void yukle(){
        try{
            RecordStore rs=RecordStore.openRecordStore("tabu3",true);
            if(rs.getNumRecords()>0){
                byte[] d=rs.getRecord(1);
                if(d!=null&&d.length>=22){
                    enSkor=bi(d,0);totD=bi(d,4);totT=bi(d,8);totP=bi(d,12);
                    temaIdx=d[16]&0xFF;if(temaIdx>=TM.length)temaIdx=0;
                    dil=d[17]&0xFF;if(dil>1)dil=0;
                    sesAcik=(d[18]&1)!=0;titAcik=(d[18]&2)!=0;muzikAcik=(d[18]&4)!=0;
                    int nl=d[19]&0xFF;
                    if(nl>0&&d.length>=20+nl){
                        char[] nc=new char[nl];
                        for(int i=0;i<nl;i++)nc[i]=(char)(d[20+i]&0xFF);
                        enTakim=new String(nc);
                    }
                }
            }
            rs.closeRecordStore();
        }catch(Exception e){}
    }

    private void kaydet(){
        try{
            RecordStore rs=RecordStore.openRecordStore("tabu3",true);
            char[] nc=enTakim.toCharArray();
            int nl=nc.length>20?20:nc.length;
            byte[] d=new byte[20+nl];
            ib(enSkor,d,0);ib(totD,d,4);ib(totT,d,8);ib(totP,d,12);
            d[16]=(byte)temaIdx;d[17]=(byte)dil;
            d[18]=(byte)((sesAcik?1:0)|(titAcik?2:0)|(muzikAcik?4:0));
            d[19]=(byte)nl;
            for(int i=0;i<nl;i++)d[20+i]=(byte)nc[i];
            if(rs.getNumRecords()==0)rs.addRecord(d,0,d.length);
            else rs.setRecord(1,d,0,d.length);
            rs.closeRecordStore();
        }catch(Exception e){}
    }

    private static int bi(byte[] b,int o){return((b[o]&0xFF)<<24)|((b[o+1]&0xFF)<<16)|((b[o+2]&0xFF)<<8)|(b[o+3]&0xFF);}
    private static void ib(int v,byte[] b,int o){b[o]=(byte)(v>>24);b[o+1]=(byte)(v>>16);b[o+2]=(byte)(v>>8);b[o+3]=(byte)v;}

    // ── Game loop ─────────────────────────────────────────────────────────────
    public void run(){
        while(running){
            W=getWidth();H=getHeight();T=TM[temaIdx];
            if(turAktif){
                kSure=turSuresi-(int)((System.currentTimeMillis()-turStart)/1000);
                if(kSure<=0){kSure=0;turBit();}
                else if(kSure<=10)bipSes();
            }
            if(ekran==S_FLASH&&System.currentTimeMillis()-flashStart>900)ekran=S_OYUN;
            // Kart animasyonu
            if(animYon!=0){
                long el=System.currentTimeMillis()-animT;
                if(el<300){
                    // Ease-out cubic animasyon
                    double t2=1.0-el/300.0;
                    kartOff=animYon*(int)(W*t2*t2*t2);
                } else {kartOff=0;animYon=0;}
            }
            repaint();serviceRepaints();
            try{Thread.sleep(60);}catch(InterruptedException e){}
        }
    }

    // ── Paint ─────────────────────────────────────────────────────────────────
    protected void paint(Graphics g){
        W=getWidth();H=getHeight();T=TM[temaIdx];
        fR(g,T[0],0,0,W,H);
        switch(ekran){
            case S_MENU:    pMenu(g);   break;
            case S_AYAR:    pAyar(g);   break;
            case S_TAKIM_AD:pTakimAd(g);break;
            case S_HAZIR:   pHazir(g);  break;
            case S_OYUN:    pOyun(g);   break;
            case S_FLASH:   pFlash(g);  break;
            case S_TUR_BIT: pTurBit(g); break;
            case S_KAZANAN: pKazan(g);  break;
            case S_ISTAT:   pIstat(g);  break;
        }
    }

    // ── MENU ─────────────────────────────────────────────────────────────────
    private void pMenu(Graphics g){
        Font fL=fl(Font.SIZE_LARGE),fM=fl(Font.SIZE_MEDIUM);
        Font fS=fp(Font.SIZE_SMALL),fSB=fb(Font.SIZE_SMALL);
        long now=System.currentTimeMillis();

        // Ust alan - T harfi
        fR(g,T[2],0,0,W,H/3);
        int px=W/2-30,py=10,ps=9;
        g.setColor(T[10]);g.fillRect(px,py,60,ps*2);g.fillRect(px+22,py+ps*2,16,ps*5);
        g.setColor(drk(T[10]));g.fillRect(px+2,py+2,58,ps*2-2);g.fillRect(px+24,py+ps*2+2,14,ps*5-2);
        g.setColor(lgt(T[10]));g.fillRect(px+4,py+2,10,4);
        int lh=H/3;

        // TABU - renk animasyonu
        int tc=blnd(T[10],T[11],(int)(128+127*Math.sin(now/500.0)));
        g.setColor(tc);g.setFont(fL);ciz(g,dil==0?"TABU":"TABOO",W/2,lh+2);

        // Bayrak + dil etiketi
        int flagY=lh+4;
        Image flagImg=dil==0?imgTR:imgEN;
        if(flagImg!=null){
            int fw=flagImg.getWidth(),fh=flagImg.getHeight();
            // Bayrak saga yasla
            g.drawImage(flagImg,W-fw-5,flagY,Graphics.TOP|Graphics.LEFT);
        } else {
            // Yedek metin
            g.setColor(dil==0?0xFF3333:0x3333FF);g.setFont(fSB);
            String dl=dil==0?"[TR]":"[EN]";
            g.drawString(dl,W-fSB.stringWidth(dl)-5,flagY,Graphics.TOP|Graphics.LEFT);
        }

        // 10000+ KELIME - zipla
        int by=(int)(3*Math.sin(now/300.0));
        int br=(int)(200+55*Math.sin(now/400.0));
        g.setColor((br<<16)|((br*200/255)<<8)|0);
        g.setFont(fSB);ciz(g,dil==0?"10.000+ KELIME":"10,000+ WORDS",W/2,lh+fL.getHeight()+7+by);

        // Rekor
        if(enSkor>0){
            g.setColor(T[11]);g.setFont(fSB);
            ciz(g,cat4("Rekor: ",iv(enSkor)," - ",enTakim),W/2,lh+fL.getHeight()+fSB.getHeight()+13);
        }

        String[] items=dil==0
            ?new String[]{"OYUNA BASLA","AYARLAR","TAKIM ADLARI","ISTATISTIK","CIKIS"}
            :new String[]{"START GAME","SETTINGS","TEAM NAMES","STATISTICS","EXIT"};
        int btnH=Math.max(24,H/11),btnW=W*4/5,bX=(W-btnW)/2;
        int sY=lh+fL.getHeight()+fSB.getHeight()+(enSkor>0?fSB.getHeight()+6:0)+14;
        for(int i=0;i<items.length;i++){
            int bY=sY+i*(btnH+4);
            boolean sel=(i==menuSec);
            int bc=sel?blnd(T[4],T[11],(int)(50+30*Math.sin(now/200.0))):T[2];
            fRnd(g,bc,bX,bY,btnW,btnH);
            g.setColor(T[3]);g.drawRoundRect(bX,bY,btnW,btnH,8,8);
            g.setColor(sel?T[7]:T[8]);g.setFont(fM);
            ciz(g,items[i],W/2,bY+btnH/2-fM.getHeight()/2);
        }
        g.setColor(T[9]);g.setFont(fS);
        ciz(g,dil==0?"2/8:Sec  5:Gir":"2/8:Sel  5:Go",W/2,H-fS.getHeight()-2);
    }

    // ── AYARLAR ──────────────────────────────────────────────────────────────
    private void pAyar(Graphics g){
        Font fM=fl(Font.SIZE_MEDIUM),fS=fp(Font.SIZE_SMALL),fSB=fb(Font.SIZE_SMALL);
        fR(g,T[2],0,0,W,30);
        g.setColor(T[10]);g.setFont(fM);
        ciz(g,dil==0?"AYARLAR":"SETTINGS",W/2,7);
        g.setColor(T[8]);g.setFont(fS);
        g.drawString(dil==0?"* Geri":"* Back",5,8,Graphics.TOP|Graphics.LEFT);

        String[] bas=dil==0
            ?new String[]{"TAKIM","SURE","PAS","TUR","DIL","TEMA","SES","TITRESIM","MUZIK"}
            :new String[]{"TEAMS","TIME","SKIP","ROUNDS","LANGUAGE","THEME","SOUND","VIBRATION","MUSIC"};

        String pv=PAS[pasIdx]==99?(dil==0?"Sinir.":"Unlim."):cat(iv(PAS[pasIdx]),dil==0?" pas":" sk");
        String tv=TUR[turIdx]==99?(dil==0?"Sinir.":"Unlim."):cat(iv(TUR[turIdx]),dil==0?" tur":" r");

        // Dil satiri icin bayrak
        String dilStr=dil==0?"< Turkce >":"< English >";

        String[] deg={
            cat3(takimS>2?"< ":" ",iv(takimS),takimS<6?" >":" "),
            cat3(sureIdx>0?"< ":" ",iv(turSuresi),sureIdx<SUR.length-1?dil==0?" sn >":" s >":(dil==0?" sn":" s")),
            cat3(pasIdx>0?"< ":" ",pv,pasIdx<PAS.length-1?" >":" "),
            cat3(turIdx>0?"< ":" ",tv,turIdx<TUR.length-1?" >":" "),
            dilStr,
            cat3(temaIdx>0?"< ":" ",TM_AD[temaIdx][dil],temaIdx<TM.length-1?" >":" "),
            sesAcik?(dil==0?"ACIK":"ON"):(dil==0?"KAPALI":"OFF"),
            titAcik?(dil==0?"ACIK":"ON"):(dil==0?"KAPALI":"OFF"),
            muzikAcik?(dil==0?"ACIK":"ON"):(dil==0?"KAPALI":"OFF")
        };

        int rH=Math.max(24,(H-34)/10),rW=W-20,rX=10;
        for(int i=0;i<9;i++){
            int rY=34+i*(rH+2);
            boolean sel=(ayarSec==i);
            fRnd(g,sel?T[2]:T[1],rX,rY,rW,rH);
            if(sel){g.setColor(T[10]);g.drawRoundRect(rX,rY,rW,rH,6,6);}
            g.setColor(T[8]);g.setFont(fS);
            g.drawString(bas[i],rX+6,rY+rH/2-fS.getHeight()/2,Graphics.TOP|Graphics.LEFT);

            // Dil satirinda bayrak ciz
            if(i==4&&(imgTR!=null||imgEN!=null)){
                Image fi=dil==0?imgTR:imgEN;
                if(fi!=null){
                    int fy=rY+rH/2-fi.getHeight()/2;
                    g.drawImage(fi,rX+rW-fi.getWidth()-8,fy,Graphics.TOP|Graphics.LEFT);
                }
            }

            int vc=(i==6)?((sesAcik)?T[5]:T[4]):(i==7)?((titAcik)?T[5]:T[4]):(i==8)?((muzikAcik)?T[5]:T[4]):T[7];
            g.setColor(vc);g.setFont(fSB);
            // Dil satirinda bayrak varsa degeri sola yaz
            if(i==4&&(imgTR!=null||imgEN!=null)){
                ciz(g,deg[i],W/2,rY+rH/2-fSB.getHeight()/2);
            } else {
                g.drawString(deg[i],rX+rW-fSB.stringWidth(deg[i])-8,rY+rH/2-fSB.getHeight()/2,Graphics.TOP|Graphics.LEFT);
            }
        }
        g.setColor(T[9]);g.setFont(fS);
        ciz(g,"2/8:Satir  4/6:Deger  *:Kapat",W/2,H-fS.getHeight()-2);
    }

    // ── HAZIR ─────────────────────────────────────────────────────────────────
    private void pHazir(Graphics g){
        Font fL=fl(Font.SIZE_LARGE),fM=fl(Font.SIZE_MEDIUM),fS=fp(Font.SIZE_SMALL);
        long now=System.currentTimeMillis();
        int mx=TUR[turIdx];
        if(mx!=99){g.setColor(T[8]);g.setFont(fS);ciz(g,cat3("Tur ",iv(mTur),cat("/",iv(mx))),W/2,8);}
        int tc=blnd(T[10],T[5],(int)(128+127*Math.sin(now/400.0)));
        g.setColor(tc);g.setFont(fL);ciz(g,takimAd[aktif],W/2,H/5);
        g.setColor(T[8]);g.setFont(fM);
        ciz(g,cat(dil==0?"Puan: ":"Score: ",iv(toplamPuan[aktif])),W/2,H/5+fL.getHeight()+4);
        int iy=H/2-30;
        g.setColor(T[7]);g.setFont(fM);
        ciz(g,cat4(dil==0?"Sure: ":"Time: ",iv(turSuresi),dil==0?" sn":" s",""),W/2,iy);iy+=fM.getHeight()+4;
        String ps=PAS[pasIdx]==99?(dil==0?"Sinir.":"Unlim."):iv(PAS[pasIdx]);
        ciz(g,cat(dil==0?"Pas: ":"Skip: ",ps),W/2,iy);iy+=fM.getHeight()+4;
        g.setColor(T[4]);ciz(g,dil==0?"Tabu = -3 Puan!":"Taboo = -3 Points!",W/2,iy);
        int bW=W*2/3,bH=36;
        int gc=blnd(T[5],T[10],(int)(128+127*Math.sin(now/300.0)));
        fRnd(g,gc,(W-bW)/2,H*3/4,bW,bH);
        g.setColor(T[1]);g.setFont(fM);ciz(g,dil==0?"BASLAT [5]":"START [5]",W/2,H*3/4+bH/2-fM.getHeight()/2);
        g.setColor(T[9]);g.setFont(fS);ciz(g,"* = Menu",W/2,H-fS.getHeight()-2);
    }

    // ── OYUN ─────────────────────────────────────────────────────────────────
    private void pOyun(Graphics g){
        Font fL=fl(Font.SIZE_LARGE),fM=fl(Font.SIZE_MEDIUM);
        Font fS=fp(Font.SIZE_SMALL),fSB=fb(Font.SIZE_SMALL);
        int barH=26;
        fR(g,T[2],0,0,W,barH);
        g.setFont(fM);
        if(kSure<=10)g.setColor(T[4]);
        else if(kSure<=20)g.setColor(T[6]);
        else g.setColor(T[5]);
        g.drawString(cat(iv(kSure),"s"),6,4,Graphics.TOP|Graphics.LEFT);
        g.setColor(T[7]);g.setFont(fSB);
        ciz(g,cat4(takimAd[aktif],"  +",tDogru," -").concat(iv(tTabu*3)),W/2,6);
        if(PAS[pasIdx]!=99){
            g.setColor(kPas<=1?T[4]:T[11]);g.setFont(fS);
            String ps=cat("P:",iv(kPas));
            g.drawString(ps,W-fS.stringWidth(ps)-5,7,Graphics.TOP|Graphics.LEFT);
        }
        int btnH=28,hH=fS.getHeight()+4;
        int kY=barH+4,kH=H-kY-btnH-hH-8;
        int kX=6+kartOff,kW=W-12;
        fRnd(g,T[1],kX,kY,kW,kH);
        g.setColor(T[4]);g.drawRoundRect(kX,kY,kW,kH,10,10);
        int idx=deste[dIdx];
        String ana=dil==0?TabuData.ana(idx):EngData.ana(idx);
        g.setColor(T[7]);g.setFont(fL);ciz(g,ana,W/2+kartOff,kY+8);
        int ay=kY+8+fL.getHeight()+6;
        g.setColor(T[4]);g.fillRect(kX+15,ay,kW-30,2);
        g.setFont(fSB);ciz(g,dil==0?"-- YASAK --":"-- FORBIDDEN --",W/2+kartOff,ay+4);
        int yb=ay+fSB.getHeight()+8,ar=(kH-(yb-kY)-4)/5;
        for(int i=0;i<5;i++){
            int yw=yb+i*ar;
            fRnd(g,T[2],kX+8,yw,kW-16,ar-3);
            g.setColor(T[7]);g.setFont(fM);
            ciz(g,dil==0?TabuData.yasak(idx,i):EngData.yasak(idx,i),W/2+kartOff,yw+(ar-3)/2-fM.getHeight()/2);
        }
        int btnY=H-btnH-hH-2,bW3=(W-12)/3;
        fRnd(g,0x8B0000,4,btnY,bW3,btnH);
        fRnd(g,0x1A5E1A,4+bW3+2,btnY,bW3,btnH);
        boolean pk=PAS[pasIdx]==99||kPas>0;
        fRnd(g,pk?0x5E4A00:T[3],4+(bW3+2)*2,btnY,bW3,btnH);
        g.setColor(T[7]);g.setFont(fSB);
        ciz(g,dil==0?"TABU[1]":"TABOO[1]",4+bW3/2,btnY+btnH/2-fSB.getHeight()/2);
        ciz(g,dil==0?"DOGRU[5]":"RIGHT[5]",4+bW3+2+bW3/2,btnY+btnH/2-fSB.getHeight()/2);
        g.setColor(pk?T[7]:T[8]);
        ciz(g,dil==0?"PAS[3]":"SKIP[3]",4+(bW3+2)*2+bW3/2,btnY+btnH/2-fSB.getHeight()/2);
        g.setColor(T[9]);g.setFont(fS);
        ciz(g,dil==0?"1=Tabu  5=Dogru  3=Pas  *=Dur":"1=Taboo  5=Right  3=Skip  *=Stop",W/2,H-hH);
    }

    // ── FLASH ────────────────────────────────────────────────────────────────
    private void pFlash(Graphics g){
        Font fL=fl(Font.SIZE_LARGE),fM=fl(Font.SIZE_MEDIUM);
        long t=System.currentTimeMillis()-flashStart;
        int r=(int)(0xAA+0x55*Math.abs(Math.sin(t*Math.PI/300.0)));
        if(r>255)r=255;fR(g,(r<<16),0,0,W,H);
        g.setColor(0xFFFFFF);g.setFont(fL);ciz(g,dil==0?"TABU!":"TABOO!",W/2,H/2-fL.getHeight());
        g.setFont(fM);ciz(g,"-3 Puan",W/2,H/2+10);
    }

    // ── TUR BITTI ────────────────────────────────────────────────────────────
    private void pTurBit(Graphics g){
        Font fM=fl(Font.SIZE_MEDIUM),fS=fp(Font.SIZE_SMALL);
        fR(g,T[2],0,0,W,34);
        g.setColor(T[10]);g.setFont(fM);
        ciz(g,cat(dil==0?"TUR BITTI - ":"ROUND OVER - ",takimAd[aktif]),W/2,9);
        int y=44;
        g.setColor(T[7]);g.setFont(fM);
        ciz(g,cat4(dil==0?"Dogru: ":"Right: ",iv(tDogru),"  Tabu: ",iv(tTabu)),W/2,y);y+=fM.getHeight()+6;
        int net=tDogru-tTabu*3;
        g.setColor(net>=0?T[5]:T[4]);
        ciz(g,cat(dil==0?"Bu tur: ":"Round: ",cat(net>=0?"+":"",cat(iv(net)," p"))),W/2,y);y+=fM.getHeight()+12;
        g.setColor(T[8]);g.setFont(fS);ciz(g,dil==0?"-- PUAN --":"-- SCORE --",W/2,y);y+=fS.getHeight()+5;
        int rH=Math.max(22,H/15);
        for(int i=0;i<takimS;i++){
            boolean ben=(i==aktif);
            fRnd(g,ben?blnd(T[2],T[5],60):T[2],10,y,W-20,rH);
            if(ben){g.setColor(T[5]);g.drawRoundRect(10,y,W-20,rH,6,6);}
            g.setColor(ben?T[5]:T[7]);g.setFont(fM);
            ciz(g,cat4(takimAd[i],": ",toplamPuan[i]," p"),W/2,y+rH/2-fM.getHeight()/2);
            y+=rH+4;
        }
        y+=5;int bW=W-20,bH=24;
        boolean bitti=oyunBittiMi();
        fRnd(g,bitti?T[11]:T[5],10,y,bW,bH);
        g.setColor(T[1]);g.setFont(fM);
        ciz(g,bitti?(dil==0?"OYUN BITTI [5]":"GAME OVER [5]"):(dil==0?"SONRAKI [5]":"NEXT [5]"),W/2,y+bH/2-fM.getHeight()/2);
        y+=bH+5;fRnd(g,T[2],10,y,bW,bH);g.setColor(T[7]);
        ciz(g,dil==0?"MENU [*]":"MENU [*]",W/2,y+bH/2-fM.getHeight()/2);
    }

    // ── KAZANAN ──────────────────────────────────────────────────────────────
    private void pKazan(Graphics g){
        Font fL=fl(Font.SIZE_LARGE),fM=fl(Font.SIZE_MEDIUM);
        Font fS=fp(Font.SIZE_SMALL),fSB=fb(Font.SIZE_SMALL);
        long now=System.currentTimeMillis();
        fR(g,T[0],0,0,W,H);
        // Konfeti
        Random rnd=new Random((now/150)%1000);
        int[] cs={T[4],T[5],T[10],T[11],T[6]};
        for(int i=0;i<25;i++){
            int cx=Math.abs(rnd.nextInt())%W,cy=Math.abs(rnd.nextInt())%H;
            g.setColor(cs[Math.abs(rnd.nextInt())%cs.length]);
            g.fillRect(cx,cy,6,6);
        }
        g.setColor(T[10]);g.setFont(fL);ciz(g,dil==0?"KAZANAN!":"WINNER!",W/2,H/8);
        int tc=blnd(T[10],T[11],(int)(128+127*Math.sin(now/300.0)));
        g.setColor(tc);g.setFont(fL);ciz(g,takimAd[kazIdx],W/2,H/8+fL.getHeight()+8);
        g.setColor(T[10]);g.setFont(fM);ciz(g,cat(iv(toplamPuan[kazIdx])," puan"),W/2,H/8+fL.getHeight()*2+12);
        if(toplamPuan[kazIdx]>=enSkor){g.setColor(T[11]);g.setFont(fM);ciz(g,dil==0?"YENi REKOR!":"NEW RECORD!",W/2,H/2-10);}
        int y=H/2+20;
        for(int i=0;i<takimS;i++){
            boolean kaz=(i==kazIdx);
            g.setColor(kaz?T[10]:T[8]);g.setFont(kaz?fSB:fS);
            ciz(g,cat4(kaz?">> ":"",takimAd[i],": ",cat(iv(toplamPuan[i])," p")),W/2,y);
            y+=fS.getHeight()+4;
        }
        int bW=W-20,bH=26;
        fRnd(g,T[5],10,H-bH*2-12,bW,bH);g.setColor(T[1]);g.setFont(fM);
        ciz(g,dil==0?"TEKRAR OYNA [5]":"PLAY AGAIN [5]",W/2,H-bH*2-12+bH/2-fM.getHeight()/2);
        fRnd(g,T[2],10,H-bH-4,bW,bH);g.setColor(T[7]);
        ciz(g,dil==0?"ANA MENU [*]":"MAIN MENU [*]",W/2,H-bH-4+bH/2-fM.getHeight()/2);
    }

    // ── ISTATISTIK ───────────────────────────────────────────────────────────
    private void pIstat(Graphics g){
        Font fM=fl(Font.SIZE_MEDIUM),fS=fp(Font.SIZE_SMALL),fSB=fb(Font.SIZE_SMALL);
        fR(g,T[2],0,0,W,30);
        g.setColor(T[10]);g.setFont(fM);ciz(g,dil==0?"ISTATISTIK":"STATISTICS",W/2,7);
        int y=38,rH=26,rW=W-20,rX=10;
        String[][] rows=dil==0?new String[][]{
            {"Toplam Dogru",iv(totD)},{"Toplam Tabu",iv(totT)},
            {"Toplam Pas",iv(totP)},{"En Yuksek",iv(enSkor)},{"Rekor",enTakim}
        }:new String[][]{
            {"Total Right",iv(totD)},{"Total Taboo",iv(totT)},
            {"Total Skip",iv(totP)},{"High Score",iv(enSkor)},{"Record",enTakim}
        };
        for(int i=0;i<rows.length;i++){
            fRnd(g,T[1],rX,y,rW,rH);
            g.setColor(T[8]);g.setFont(fS);g.drawString(rows[i][0],rX+8,y+5,Graphics.TOP|Graphics.LEFT);
            g.setColor(T[7]);g.setFont(fSB);g.drawString(rows[i][1],rX+rW-fSB.stringWidth(rows[i][1])-8,y+5,Graphics.TOP|Graphics.LEFT);
            y+=rH+4;
        }
        y+=5;g.setColor(T[8]);g.setFont(fS);ciz(g,dil==0?"-- SON OYUNLAR --":"-- RECENT --",W/2,y);y+=fS.getHeight()+3;
        for(int i=0;i<5;i++){
            if(gecmis[i]!=null&&gecmis[i].length()>0){
                g.setColor(T[9]);g.setFont(fS);ciz(g,gecmis[i],W/2,y);y+=fS.getHeight()+2;
            }
        }
        int bW=W-20,bH=24;
        fRnd(g,0x5E0000,10,H-bH-5,bW,bH);g.setColor(T[7]);g.setFont(fM);
        ciz(g,dil==0?"SIFIRLA [#]":"RESET [#]",W/2,H-bH-5+bH/2-fM.getHeight()/2);
        g.setColor(T[9]);g.setFont(fS);ciz(g,"* = Menu",W/2,H-fS.getHeight()-2);
    }

    // ── TUS ──────────────────────────────────────────────────────────────────
    protected void keyPressed(int k){
        int a=getGameAction(k);
        switch(ekran){
            case S_MENU:    mTus(k,a);  break;
            case S_AYAR:    aTus(k,a);  break;
            case S_TAKIM_AD:taTus(k,a); break;
            case S_HAZIR:   hTus(k,a);  break;
            case S_OYUN:    oTus(k,a);  break;
            case S_TUR_BIT: tbTus(k,a); break;
            case S_KAZANAN: kzTus(k,a); break;
            case S_ISTAT:   iTus(k,a);  break;
        }
        repaint();
    }

    private void mTus(int k,int a){
        if(a==UP||k==K2)menuSec=(menuSec-1+5)%5;
        if(a==DOWN||k==K8)menuSec=(menuSec+1)%5;
        if(a==FIRE||k==K5){
            switch(menuSec){
                case 0:oyunBaslat();break;
                case 1:ekran=S_AYAR;ayarSec=0;break;
                case 2:adAc();break;
                case 3:ekran=S_ISTAT;break;
                case 4:muzikDur();midlet.exit();break;
            }
        }
    }

    private void aTus(int k,int a){
        if(k==KSTAR){kaydet();ekran=S_MENU;return;}
        if(a==UP||k==K2)ayarSec=(ayarSec-1+9)%9;
        if(a==DOWN||k==K8)ayarSec=(ayarSec+1)%9;
        switch(ayarSec){
            case 0:if(a==LEFT||k==K4){if(takimS>2)takimS--;}if(a==RIGHT||k==K6){if(takimS<6)takimS++;}break;
            case 1:if(a==LEFT||k==K4){if(sureIdx>0){sureIdx--;turSuresi=SUR[sureIdx];}}if(a==RIGHT||k==K6){if(sureIdx<SUR.length-1){sureIdx++;turSuresi=SUR[sureIdx];}}break;
            case 2:if(a==LEFT||k==K4){if(pasIdx>0)pasIdx--;}if(a==RIGHT||k==K6){if(pasIdx<PAS.length-1)pasIdx++;}break;
            case 3:if(a==LEFT||k==K4){if(turIdx>0)turIdx--;}if(a==RIGHT||k==K6){if(turIdx<TUR.length-1)turIdx++;}break;
            case 4:if(a==LEFT||k==K4||a==RIGHT||k==K6){dil=1-dil;}break;
            case 5:if(a==LEFT||k==K4){if(temaIdx>0)temaIdx--;}if(a==RIGHT||k==K6){if(temaIdx<TM.length-1)temaIdx++;}T=TM[temaIdx];break;
            case 6:if(a==FIRE||k==K5||a==LEFT||k==K4||a==RIGHT||k==K6)sesAcik=!sesAcik;break;
            case 7:if(a==FIRE||k==K5||a==LEFT||k==K4||a==RIGHT||k==K6)titAcik=!titAcik;break;
            case 8:if(a==FIRE||k==K5||a==LEFT||k==K4||a==RIGHT||k==K6){muzikAcik=!muzikAcik;if(muzikAcik)muzikBaslat();else muzikDur();}break;
        }
    }

    private void hTus(int k,int a){
        if(k==KSTAR){ekran=S_MENU;return;}
        if(a==FIRE||k==K5)turBaslat();
    }

    private void oTus(int k,int a){
        if(!turAktif)return;
        if(k==KSTAR){turDur();return;}
        if(a==FIRE||k==K5){dogruYap();return;}
        if(k==K1||a==LEFT||k==-6||k==-21){tabuYap();return;}
        if(k==K3||a==RIGHT||k==-7||k==-22||k==K9){pasYap();}
    }

    private void tbTus(int k,int a){
        if(k==KSTAR){ekran=S_MENU;return;}
        if(a==FIRE||k==K5||a==DOWN||k==K8){
            if(oyunBittiMi())oyunBitti();else sonrakiTakim();
        }
    }

    private void kzTus(int k,int a){
        if(k==KSTAR){ekran=S_MENU;return;}
        if(a==FIRE||k==K5)oyunBaslat();
    }

    private void iTus(int k,int a){
        if(k==KSTAR){ekran=S_MENU;return;}
        if(k==KHASH){totD=0;totT=0;totP=0;enSkor=0;enTakim="---";for(int i=0;i<5;i++)gecmis[i]="";kaydet();}
    }

    // ── Takim Adi Listesi ────────────────────────────────────────────────────
    private void pTakimAd(Graphics g){
        Font fM=fl(Font.SIZE_MEDIUM),fS=fp(Font.SIZE_SMALL),fSB=fb(Font.SIZE_SMALL);
        fR(g,T[2],0,0,W,30);
        g.setColor(T[10]);g.setFont(fM);
        ciz(g,dil==0?"TAKIM ADLARI":"TEAM NAMES",W/2,7);
        g.setColor(T[8]);g.setFont(fS);
        g.drawString(dil==0?"* Geri":"* Back",5,8,Graphics.TOP|Graphics.LEFT);
        g.drawString(dil==0?"5=Duzenle":"5=Edit",W-fS.stringWidth(dil==0?"5=Duzenle":"5=Edit")-5,8,Graphics.TOP|Graphics.LEFT);

        int rH=Math.max(32,H/9),rW=W-20,rX=10;
        for(int i=0;i<takimS;i++){
            int rY=36+i*(rH+5);
            boolean sel=(i==adSec);
            fRnd(g,sel?T[2]:T[1],rX,rY,rW,rH);
            if(sel){g.setColor(T[10]);g.drawRoundRect(rX,rY,rW,rH,8,8);}
            // Takim numarasi
            g.setColor(T[8]);g.setFont(fS);
            g.drawString(cat(dil==0?"Takim ":"Team ",iv(i+1)),rX+8,rY+4,Graphics.TOP|Graphics.LEFT);
            // Takim adi
            g.setColor(sel?T[7]:T[8]);g.setFont(fM);
            ciz(g,takimAd[i],W/2,rY+rH/2-fM.getHeight()/2+2);
            // Secili ise duzenleme oku
            if(sel){
                g.setColor(T[10]);g.setFont(fSB);
                g.drawString(">",rX+rW-20,rY+rH/2-fSB.getHeight()/2,Graphics.TOP|Graphics.LEFT);
            }
        }
        g.setColor(T[9]);g.setFont(fS);
        ciz(g,"2/8:Sec  5:Duzenle  *:Geri",W/2,H-fS.getHeight()-2);
    }

    private void taTus(int k,int a){
        if(k==KSTAR){ekran=S_MENU;return;}
        if(a==UP||k==K2)adSec=(adSec-1+takimS)%takimS;
        if(a==DOWN||k==K8)adSec=(adSec+1)%takimS;
        if(a==FIRE||k==K5){adTakim=adSec;adTextBoxAc();}
    }

    private void adAc(){adSec=0;ekran=S_TAKIM_AD;}

    private void adTextBoxAc(){
        String bas=cat4(dil==0?"Takim ":"Team ",iv(adTakim+1),dil==0?" Adi":" Name","");
        TextBox tb=new TextBox(bas,takimAd[adTakim],20,TextField.ANY);
        Command ok=new Command(dil==0?"Kaydet":"Save",Command.OK,1);
        Command geri=new Command(dil==0?"Geri":"Back",Command.BACK,3);
        tb.addCommand(ok);tb.addCommand(geri);
        final TextBox ftb=tb;
        tb.setCommandListener(new CommandListener(){
            public void commandAction(Command c,Displayable d){
                String s=ftb.getString().trim();
                if(s.length()>0)takimAd[adTakim]=s;
                midlet.getDisplay().setCurrent(GameCanvas.this);
                ekran=S_TAKIM_AD;
            }
        });
        midlet.getDisplay().setCurrent(tb);
    }

    // ── Oyun Mantigi ─────────────────────────────────────────────────────────
    private void oyunBaslat(){
        puan=new int[6];toplamPuan=new int[6];aktif=0;mTur=1;
        int tot=dil==0?TabuData.TOPLAM:EngData.TOPLAM;
        deste=karistir(tot);dIdx=0;ekran=S_HAZIR;
    }

    private void turBaslat(){
        tDogru=0;tTabu=0;tPas=0;kPas=PAS[pasIdx];
        turStart=System.currentTimeMillis();turAktif=true;ekran=S_OYUN;
    }

    private void kartGec(int yon){animYon=yon;animT=System.currentTimeMillis();dIdx=(dIdx+1)%deste.length;}

    private void dogruYap(){tDogru++;totD++;kartGec(1);}

    private void tabuYap(){
        tTabu++;totT++;titret();
        flashStart=System.currentTimeMillis();ekran=S_FLASH;kartGec(-1);
    }

    private void pasYap(){
        if(PAS[pasIdx]==99){kartGec(1);return;}
        if(kPas>0){kPas--;tPas++;totP++;kartGec(1);}
    }

    private void turDur(){
        turAktif=false;
        int net=tDogru-tTabu*3;toplamPuan[aktif]+=net;
        if(toplamPuan[aktif]<0)toplamPuan[aktif]=0;ekran=S_TUR_BIT;
    }

    private void turBit(){
        turAktif=false;
        int net=tDogru-tTabu*3;toplamPuan[aktif]+=net;
        if(toplamPuan[aktif]<0)toplamPuan[aktif]=0;ekran=S_TUR_BIT;
    }

    private boolean oyunBittiMi(){
        return TUR[turIdx]!=99&&aktif==takimS-1&&mTur>=TUR[turIdx];
    }

    private void sonrakiTakim(){aktif=(aktif+1)%takimS;if(aktif==0)mTur++;ekran=S_HAZIR;}

    private void oyunBitti(){
        kazIdx=0;
        for(int i=1;i<takimS;i++)if(toplamPuan[i]>toplamPuan[kazIdx])kazIdx=i;
        if(toplamPuan[kazIdx]>enSkor){enSkor=toplamPuan[kazIdx];enTakim=takimAd[kazIdx];}
        String s=cat4(takimAd[kazIdx]," ",iv(toplamPuan[kazIdx]),"p");
        for(int i=4;i>0;i--)gecmis[i]=gecmis[i-1];gecmis[0]=s;
        kaydet();ekran=S_KAZANAN;
    }

    private int[] karistir(int n){
        int[] a=new int[n];for(int i=0;i<n;i++)a[i]=i;
        Random r=new Random();
        for(int i=n-1;i>0;i--){int j=(r.nextInt()&0x7FFFFFFF)%(i+1);int t=a[i];a[i]=a[j];a[j]=t;}
        return a;
    }

    // ── Yardimci ─────────────────────────────────────────────────────────────
    private Font fl(int s){return Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD,s);}
    private Font fp(int s){return Font.getFont(Font.FACE_SYSTEM,Font.STYLE_PLAIN,s);}
    private Font fb(int s){return Font.getFont(Font.FACE_SYSTEM,Font.STYLE_BOLD,s);}
    private void fR(Graphics g,int c,int x,int y,int w,int h){g.setColor(c);g.fillRect(x,y,w,h);}
    private void fRnd(Graphics g,int c,int x,int y,int w,int h){g.setColor(c);g.fillRoundRect(x,y,w,h,8,8);}
    private void ciz(Graphics g,String s,int cx,int y){g.drawString(s,cx-g.getFont().stringWidth(s)/2,y,Graphics.TOP|Graphics.LEFT);}

    // String birlestirme - char array, StringBuilder/StringBuffer YOK
    private static String iv(int i){return String.valueOf(i);}
    private static String cat(String a,String b){
        char[] ca=a.toCharArray(),cb=b.toCharArray(),r=new char[ca.length+cb.length];
        System.arraycopy(ca,0,r,0,ca.length);System.arraycopy(cb,0,r,ca.length,cb.length);
        return new String(r);
    }
    private static String cat3(String a,String b,String c){return cat(cat(a,b),c);}
    private static String cat4(String a,String b,String c,String d){return cat(cat(a,b),cat(c,d));}
    private static String cat4(String a,String b,int c,String d){return cat4(a,b,iv(c),d);}
    private static String cat4(String a,int b,String c,String d){return cat4(a,iv(b),c,d);}

    // Renk
    private static int drk(int c){return((((c>>16)&0xFF)*7/10)<<16)|((((c>>8)&0xFF)*7/10)<<8)|((c&0xFF)*7/10);}
    private static int lgt(int c){return(Math.min(255,((c>>16)&0xFF)*13/10)<<16)|(Math.min(255,((c>>8)&0xFF)*13/10)<<8)|Math.min(255,(c&0xFF)*13/10);}
    private static int blnd(int a,int b,int t){
        return((((a>>16)&0xFF)*(255-t)/255+((b>>16)&0xFF)*t/255)<<16)
             |((((a>>8)&0xFF)*(255-t)/255+((b>>8)&0xFF)*t/255)<<8)
             |(((a&0xFF)*(255-t)/255+(b&0xFF)*t/255));
    }
}
