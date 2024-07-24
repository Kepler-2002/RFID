package util;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.example.myapplication.ScanActivity;
import com.rfidread.Enumeration.*;
import com.rfidread.Interface.IAsynchronousMessage;
import com.rfidread.Models.GPI_Model;
import com.rfidread.Models.Tag_Model;
import com.rfidread.RFIDReader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.BlockingDeque;

public class RFIDReaderHelper implements IAsynchronousMessage{
    private boolean isConnected = false;
    private String connID;

    public boolean conn;

    public HashMap<String, Integer> EpcDataMap = new HashMap<>(); // 用于存储EPC数据和对应的次数

    public ArrayList<String> EpcDataList =  new ArrayList<>();

    private BlockingDeque<String> buffer;
    private HashMap<String, Boolean> SentDataMap;
    private ScanActivity scanActivity;

    private HandlerThread handlerThread;
    private Handler handler;


    // 其他成员变量
    private String [] Ports = {
            "/dev/ttyUSB2",
            "/dev/ttyUSB1",
            "/dev/ttyUSB0",
            "/dev/ttyS8",
            "/dev/ttyS7",
            "/dev/ttyS6",
            "/dev/ttyS5",
            "/dev/ttyS4",
            "/dev/ttyS3",
            "/dev/ttyS2",
            "/dev/ttyS1",
            "/dev/ttyS0"};

    public RFIDReaderHelper(BlockingDeque<String> buffer, HashMap<String, Boolean> SentDataMap, ScanActivity scanActivity) {
        this.buffer = buffer;
        this.SentDataMap = SentDataMap;
        this.scanActivity = scanActivity;
        initHandlerThread();
    }

    public void initHandlerThread() {
        handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public void stopHandlerThread() {
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void connectToReader(String port, int baudRate) {
        connID = port + ":" + baudRate;
        isConnected = RFIDReader.CreateSerialConn(connID, this);
    }

    public void disconnectFromReader() {
        if (isConnected) {
            RFIDReader.CloseConn(connID);
            isConnected = false;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public int setPowerLevel(int powerLevel) {
        // 设置读写器的功率等级
        HashMap<Integer, Integer> dicPower = new HashMap<>();
        dicPower.put(1, powerLevel);
        dicPower.put(2, powerLevel);
        dicPower.put(3, powerLevel);
        dicPower.put(4, powerLevel);
        int result = RFIDReader._Config.SetANTPowerParam(connID, dicPower);
        return result; // result = 0 代表设置成功
    }

    public String getPowerLevel (String connID) {
        return RFIDReader._Config.GetANTPowerParam2(this.connID);
    }

    private boolean isGPO1On = false; // 用于跟踪指示灯状态
    private long lastDataTimestamp = 0; // 记录上一次读取到数据的时间戳

    public void TurnLightOnAndOff(){
                // 获取当前时间戳
                long currentTimestamp = System.currentTimeMillis();
                // 如果指示灯已经亮，并且时间差小于3秒，继续保持亮的状态
                if (isGPO1On && (currentTimestamp - lastDataTimestamp < 3000)) {
                    // Do nothing, keep the light on
                } else {
                    // 否则，将指示灯点亮，然后更新时间戳
                    setGPO1State(eGPOState._High); // 点亮指示灯
                    isGPO1On = true;
                    lastDataTimestamp = currentTimestamp;
                    // 在3秒后将指示灯熄灭
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setGPO1State(eGPOState.Low); // 熄灭指示灯
                            isGPO1On = false;
                        }
                    }, 3000);
                }
    }

    public void setGPO1State(eGPOState state) {
        // 使用 SetReaderGPOState 函数设置 eGPO1 的状态
        HashMap<eGPO, eGPOState> gpoStates = new HashMap<>();
        gpoStates.put(eGPO._1, state);
        RFIDReader._Config.SetReaderGPOState(connID, gpoStates);
    }

    // 其他操作，例如读取标签数据等

    @Override
    public void OutPutTags(Tag_Model tag_model) {
        // 获取标签的EPC数据

        Log.d("Data Callback", "OutPutTags called with EPC: " + tag_model._EPC);
        if(EpcDataMap.get(tag_model._EPC) == null) {
            EpcDataMap.put(tag_model._EPC, 1);
            EpcDataList.add(tag_model._EPC);
        }else {
            EpcDataMap.put(tag_model._EPC, EpcDataMap.get(tag_model._EPC)  + 1);
        }

    }

    @Override
    public void OutPutTagsOver(String s) {
        Log.d("Callback", "Output Over, connID: " + s);
    }


    @Override
    public void GPIControlMsg(String s, GPI_Model gpi_model) {

        Log.d("Callback", "GPIControlMSg called with GPI Status: " + gpi_model.StartOrStop);

        if(gpi_model.StartOrStop == 1){ // 触发停止
            int max = 0;

            // 确定sentData
            String SentData = "";
            for (String item:EpcDataList) {
                if (EpcDataMap.get(item) != null && EpcDataMap.get(item) > max){
                    max = EpcDataMap.get(item);
                    SentData = item;
                }
            }

            // 将数据 + 当前时间发送到缓冲区, 写日志
            try{
                Date now = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
                String currentTime = formatter.format(now);

                if(max != 0 && (SentDataMap.get(SentData) == null || Boolean.FALSE.equals(SentDataMap.get(SentData))) ){ // 有数据，且这条数据没进过缓冲区
                    //推进缓冲区
                    buffer.putLast(SentData + " " + currentTime);

                    //写日志
                    LogUtils.saveLog(SentData + " " + currentTime);

                    // 更新全局已发送map
                    SentDataMap.put(SentData, true);

                    // 插入表格中并更新读取数量
                    scanActivity.updateReadCount();
                    scanActivity.insertRowInTable(SentData);
                    TurnLightOnAndOff();
                }else { //
                    //推进缓冲区
                    buffer.putLast("noread " + currentTime);
                    //写日志
                    LogUtils.saveLog("noread " + currentTime);
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }

            //重新开始计数
            EpcDataMap = new HashMap<>();
            EpcDataList = new ArrayList<>();
        }
    }

    public void closeAllConnect() {
        RFIDReader.CloseAllConnect();
    }

    @Override
    public void OutPutScanData(String s, byte[] bytes) {
        // 扫描数据输出的回调方法
    }

    @Override
    public void WriteDebugMsg(String s, String s1) {
        // 写入调试消息的回调方法
    }

    @Override
    public void WriteLog(String s, String s1) {
        // 写入日志的回调方法
    }

    @Override
    public void PortConnecting(String s) {

    }

    @Override
    public void PortClosing(String s) {

    }




    public ConnectionResult connectToReader() {
        for (int i = 0; i < Ports.length; i++) {
            conn = RFIDReader.CreateSerialConn(Ports[i] + ":115200", this);
            if (conn) {
                int finalI = i;
                connID = Ports[i] + ":115200";
                return new ConnectionResult(conn, connID); // 连接成功，返回 true
            }
        }

        return new ConnectionResult(conn, "Connection Failed"); // 连接失败，返回 false
    }
}
