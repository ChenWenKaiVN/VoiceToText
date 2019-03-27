package com.voice.text;

import java.io.FileOutputStream;
import java.util.HashMap;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.alibaba.fastjson.JSON;
import com.iflytek.msp.cpdb.lfasr.client.LfasrClientImp;
import com.iflytek.msp.cpdb.lfasr.exception.LfasrException;
import com.iflytek.msp.cpdb.lfasr.model.LfasrType;
import com.iflytek.msp.cpdb.lfasr.model.Message;
import com.iflytek.msp.cpdb.lfasr.model.ProgressStatus;
import com.iflytek.voicecloud.lfasr.demo.Test;

public class TransferThreadRunnable implements Runnable {
	
	private Text logDetailText;
	private LfasrType type = LfasrType.LFASR_STANDARD_RECORDED_AUDIO;
	private int sleepSecond = 20;
	private String voicePath;
	private Display display;
	private Button startThansfer;
	private String outputPath;
	
	public TransferThreadRunnable(Composite parent, Text logDetailText, String voicePath, String textPath, Button startThansfer, String outputPath) {
		this.logDetailText = logDetailText;		
		this.voicePath = voicePath;
		this.display = parent.getDisplay();
		this.startThansfer = startThansfer;
		this.outputPath = outputPath;
	}
	
	
	public void asycExecPrint(String log){
		display.asyncExec(new Runnable() {				
			@Override
			public void run() {		   
				logDetailText.append(log);						
			}
		});
	}
	
	public void asycExecSetStartThansferButtonStatus(boolean status){
		display.asyncExec(new Runnable() {				
			@Override
			public void run() {		   
				startThansfer.setEnabled(status);					
			}
		});
	}
	
	@Override
	public void run() {	
		transfer();	
	}
	
	private int transfer(){
		asycExecSetStartThansferButtonStatus(false);
		// 初始化LFASRClient实例
        LfasrClientImp lc = null;
        try {
            lc = LfasrClientImp.initLfasrClient();
        } catch (LfasrException e) {
            // 初始化异常，解析异常描述信息
            Message initMsg = JSON.parseObject(e.getMessage(), Message.class);
            asycExecPrint(DateUtils.getDataPrefix() + "ecode = " + initMsg.getErr_no() + "\n");
            asycExecPrint(DateUtils.getDataPrefix() + "failed = " + initMsg.getFailed() + "\n");            
            return -1;
        }

        // 获取上传任务ID
        String task_id = "";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("has_participle", "true");
        //合并后标准版开启电话版功能
        //params.put("has_seperate", "true");
        try {
            // 上传音频文件
            Message uploadMsg = lc.lfasrUpload(voicePath, type, params);

            while(true){
            	// 判断返回值
            	try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	asycExecPrint(DateUtils.getDataPrefix() + "文件正在上传......" + "\n");
                int ok = uploadMsg.getOk();
                if (ok == 0) {
                    // 创建任务成功
                    task_id = uploadMsg.getData();
                    asycExecPrint(DateUtils.getDataPrefix() + "创建任务成功  task_id = " + task_id + "\n");
                    break;
                } else {
                    // 创建任务失败-服务端异常
                    asycExecPrint(DateUtils.getDataPrefix() + "ecode = " + uploadMsg.getErr_no() + "\n");
                    asycExecPrint(DateUtils.getDataPrefix() + "failed = " + uploadMsg.getFailed() + "\n");
                    return -1;
                }
            }            
        } catch (LfasrException e) {
            // 上传异常，解析异常描述信息
            Message uploadMsg = JSON.parseObject(e.getMessage(), Message.class);
            asycExecPrint(DateUtils.getDataPrefix() + "ecode=" + uploadMsg.getErr_no() + "\n"); 
            asycExecPrint(DateUtils.getDataPrefix() + "failed=" + uploadMsg.getFailed() + "\n"); 
            return -1;
        }

        // 循环等待音频处理结果
        while (true) {
            try {
            	// 等待20s在获取任务进度
            	for(int i=0; i<sleepSecond/2; i++){
            		//每隔2秒打印一次当前状态
                    Thread.sleep(2000);
                    asycExecPrint(DateUtils.getDataPrefix() + "当前任务正在处理 " + "waiting ..." + "\n");
            	}               
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                // 获取处理进度
                Message progressMsg = lc.lfasrGetProgress(task_id);

                // 如果返回状态不等于0，则任务失败
                if (progressMsg.getOk() != 0) {
                    asycExecPrint(DateUtils.getDataPrefix() + "任务处理失败. task_id:" + task_id + "\n");
                    asycExecPrint(DateUtils.getDataPrefix() + "ecode=" + progressMsg.getErr_no() + "\n");
                    asycExecPrint(DateUtils.getDataPrefix() + "failed=" + progressMsg.getFailed() + "\n");
                    return -1;
                } else {
                    ProgressStatus progressStatus = JSON.parseObject(progressMsg.getData(), ProgressStatus.class);
                    if (progressStatus.getStatus() == 9) {
                        // 处理完成
                        asycExecPrint(DateUtils.getDataPrefix() + "任务处理完成. task_id = " + task_id + "\n");
                        break;
                    } else {
                        // 未处理完成
                        asycExecPrint(DateUtils.getDataPrefix() + "任务没有处理完成，继续处理. task_id = " + task_id + ", status:" + progressStatus.getDesc() + "\n");
                        continue;
                    }
                }
            } catch (LfasrException e) {
                // 获取进度异常处理，根据返回信息排查问题后，再次进行获取
                Message progressMsg = JSON.parseObject(e.getMessage(), Message.class);
                asycExecPrint(DateUtils.getDataPrefix() + "ecode = " + progressMsg.getErr_no() + "\n");
                asycExecPrint(DateUtils.getDataPrefix() + "failed = " + progressMsg.getFailed() + "\n");
            }
        }

        // 获取任务结果
        try {
            Message resultMsg = lc.lfasrGetResult(task_id);
            // 如果返回状态等于0，则获取任务结果成功
            if (resultMsg.getOk() == 0) {
                // 打印转写结果
            	String result = Test.getFinalResult(resultMsg.getData());
            	FileOutputStream f;
				try {
					f = new FileOutputStream(outputPath);
					f.write(result.getBytes());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}            	
                asycExecPrint(DateUtils.getDataPrefix() + "结果存放路径： " + outputPath + "\n");
            } else {
                // 获取任务结果失败
                asycExecPrint(DateUtils.getDataPrefix() + "ecode = " + resultMsg.getErr_no() + "\n");
                asycExecPrint(DateUtils.getDataPrefix() + "failed = " + resultMsg.getFailed() + "\n");
                return -1;
            }
        } catch (LfasrException e) {
            // 获取结果异常处理，解析异常描述信息
            Message resultMsg = JSON.parseObject(e.getMessage(), Message.class);
            asycExecPrint(DateUtils.getDataPrefix() + "ecode=" + resultMsg.getErr_no() + "\n");
            asycExecPrint(DateUtils.getDataPrefix() + "failed=" + resultMsg.getFailed() + "\n");
            return -1;
        }
        asycExecSetStartThansferButtonStatus(true);
		return 1;
	}
} 
