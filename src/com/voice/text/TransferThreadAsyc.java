package com.voice.text;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

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

public class TransferThreadAsyc implements Callable<Integer> {
	
	private Text logDetailText;
	private CountDownLatch countDownLatch;
	private LfasrType type = LfasrType.LFASR_STANDARD_RECORDED_AUDIO;
	private int sleepSecond = 20;
	private String datePrefix;
	private String voicePath;
	private String textPath;
	private Display display;
	private int result = 0;
	
	public TransferThreadAsyc(Composite parent, Text logDetailText, CountDownLatch countDownLatch, String datePrefix, String voicePath, String textPath) {
		this.logDetailText = logDetailText;
		this.countDownLatch = countDownLatch;
		this.datePrefix = datePrefix;
		this.voicePath = voicePath;
		this.textPath = textPath;
		this.display = parent.getDisplay();
	}
	@Override
	public Integer call() throws Exception {
		//int result = 0;
		display.asyncExec(new Runnable() {		
			@Override
			public void run() {
				result = transfer2();				
			}
		});
		return result;
	}
	
	private int transfer2(){
		for(int i=0; i<20; i++){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logDetailText.append(datePrefix + "doing" + "\n");
		}
		return 1;
	}
	
	private int transfer(){
		// 初始化LFASRClient实例
        LfasrClientImp lc = null;
        try {
            lc = LfasrClientImp.initLfasrClient();
        } catch (LfasrException e) {
            // 初始化异常，解析异常描述信息
            Message initMsg = JSON.parseObject(e.getMessage(), Message.class);
            logDetailText.append(datePrefix + "ecode=" + initMsg.getErr_no() + "\n");
            ////System.out.println("ecode=" + initMsg.getErr_no());
            logDetailText.append(datePrefix + "failed=" + initMsg.getFailed() + "\n");
            //System.out.println(datePrefix + "failed=" + initMsg.getFailed());
            countDownLatch.countDown();
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

            // 判断返回值
            int ok = uploadMsg.getOk();
            if (ok == 0) {
                // 创建任务成功
                task_id = uploadMsg.getData();
                //System.out.println("创建任务成功  task_id=" + task_id);
                logDetailText.append(datePrefix + "创建任务成功  task_id=" + task_id + "\n");
            } else {
                // 创建任务失败-服务端异常
                //System.out.println(datePrefix + "ecode=" + uploadMsg.getErr_no());
                logDetailText.append(datePrefix + "ecode=" + uploadMsg.getErr_no() + "\n");
                //System.out.println(datePrefix + "failed=" + uploadMsg.getFailed());
                logDetailText.append(datePrefix + "failed=" + uploadMsg.getFailed() + "\n");
                countDownLatch.countDown();
                return -1;
            }
        } catch (LfasrException e) {
            // 上传异常，解析异常描述信息
            Message uploadMsg = JSON.parseObject(e.getMessage(), Message.class);
            //System.out.println(datePrefix + "ecode=" + uploadMsg.getErr_no());
            logDetailText.append(datePrefix + "ecode=" + uploadMsg.getErr_no() + "\n");
            //System.out.println(datePrefix + "failed=" + uploadMsg.getFailed()); 
            logDetailText.append(datePrefix + "failed=" + uploadMsg.getFailed() + "\n"); 
            countDownLatch.countDown();
            return -1;
        }

        // 循环等待音频处理结果
        while (true) {
            try {
                // 等待20s在获取任务进度
                Thread.sleep(sleepSecond * 1000);
                //System.out.println("waiting ...");
                logDetailText.append(datePrefix + "failed=" + "waiting ..." + "\n");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                // 获取处理进度
                Message progressMsg = lc.lfasrGetProgress(task_id);

                // 如果返回状态不等于0，则任务失败
                if (progressMsg.getOk() != 0) {
                    //System.out.println("task was fail. task_id:" + task_id);
                    //System.out.println("ecode=" + progressMsg.getErr_no());
                    //System.out.println("failed=" + progressMsg.getFailed());
                    logDetailText.append(datePrefix + "task was fail. task_id:" + task_id + "\n");
                    logDetailText.append(datePrefix + "ecode=" + progressMsg.getErr_no() + "\n");
                    logDetailText.append(datePrefix + "failed=" + progressMsg.getFailed() + "\n");
                    countDownLatch.countDown();
                    return -1;
                } else {
                    ProgressStatus progressStatus = JSON.parseObject(progressMsg.getData(), ProgressStatus.class);
                    if (progressStatus.getStatus() == 9) {
                        // 处理完成
                        //System.out.println(datePrefix + "task was completed. task_id:" + task_id + "\n");
                        logDetailText.append(datePrefix + "task was completed. task_id:" + task_id + "\n");
                        break;
                    } else {
                        // 未处理完成
                        //System.out.println(datePrefix + "task is incomplete. task_id:" + task_id + ", status:" + progressStatus.getDesc() + "\n");
                        logDetailText.append(datePrefix + "task is incomplete. task_id:" + task_id + ", status:" + progressStatus.getDesc() + "\n");
                        continue;
                    }
                }
            } catch (LfasrException e) {
                // 获取进度异常处理，根据返回信息排查问题后，再次进行获取
                Message progressMsg = JSON.parseObject(e.getMessage(), Message.class);
                //System.out.println(datePrefix + "ecode=" + progressMsg.getErr_no() + "\n");
                //System.out.println(datePrefix + "failed=" + progressMsg.getFailed() + "\n");
                logDetailText.append(datePrefix + "ecode=" + progressMsg.getErr_no() + "\n");
                logDetailText.append(datePrefix + "failed=" + progressMsg.getFailed() + "\n");
            }
        }

        // 获取任务结果
        try {
            Message resultMsg = lc.lfasrGetResult(task_id);
            // 如果返回状态等于0，则获取任务结果成功
            if (resultMsg.getOk() == 0) {
                // 打印转写结果
            	String result = Test.getFinalResult(resultMsg.getData());
            	String output = textPath + "\\" + System.currentTimeMillis() + ".txt";
            	FileOutputStream f;
				try {
					f = new FileOutputStream(output);
					f.write(result.getBytes());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}            	
                //System.out.println(result);
                logDetailText.append(datePrefix + "结果存放路径： " + output + "\n");
                logDetailText.append(datePrefix + "最终转换结果： " + "\n");
                logDetailText.append(datePrefix + result + "\n");
            } else {
                // 获取任务结果失败
                //System.out.println(datePrefix + "ecode=" + resultMsg.getErr_no() + "\n");
                //System.out.println(datePrefix + "failed=" + resultMsg.getFailed() + "\n");
                logDetailText.append(datePrefix + "ecode=" + resultMsg.getErr_no() + "\n");
                logDetailText.append(datePrefix + "failed=" + resultMsg.getFailed() + "\n");
                countDownLatch.countDown();
                return -1;
            }
        } catch (LfasrException e) {
            // 获取结果异常处理，解析异常描述信息
            Message resultMsg = JSON.parseObject(e.getMessage(), Message.class);
            //System.out.println(datePrefix + "ecode=" + resultMsg.getErr_no() + "\n");
            //System.out.println(datePrefix + "failed=" + resultMsg.getFailed() + "\n");
            logDetailText.append(datePrefix + "ecode=" + resultMsg.getErr_no() + "\n");
            logDetailText.append(datePrefix + "failed=" + resultMsg.getFailed() + "\n");
            countDownLatch.countDown();
            return -1;
        }
		countDownLatch.countDown();
		return 1;
	}
} 
