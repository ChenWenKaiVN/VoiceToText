package com.voice.text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.wb.swt.SWTResourceManager;

public class VoiceToText extends ApplicationWindow {
	
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private String datePrefix = df.format(new Date()) + ":  ";
	final CountDownLatch countDownLatch = new CountDownLatch(1);
	private String voicePath;
	private String textPath;
	private Text voicePathText;
	private Text textPathText;
	private Text logDetailText;
	private Text logText;

	/**
	 * Create the application window.
	 */
	public VoiceToText() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	/**
	 * Create contents of the application window.
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		
		//输入目录按钮以及路径设置
		Button voiceInputButton = new Button(container, SWT.NONE);
		voiceInputButton.setBounds(123, 47, 117, 27);
		voiceInputButton.setText("输入音频文件");
		voiceInputButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {				
				FileDialog filedlg = new FileDialog((Shell) parent, SWT.OPEN);
				filedlg.setText("输入音频文件选择");
				filedlg.setFilterPath("SystemRoot");
				String selected = filedlg.open();
				if(selected == null){
					return;
				}
				voicePathText.setText(selected);
				String log = datePrefix + "您选中的音频文件路径为：" + selected;
				System.out.println(log);
				logDetailText.append(log + "\n");
			}
		});
		
		
		voicePathText = new Text(container, SWT.BORDER);
		voicePathText.setBounds(280, 49, 482, 23);
		
		//输出文本以及路径设置
		Button textOutputButton = new Button(container, SWT.NONE);
		textOutputButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog folderdlg = new DirectoryDialog((Shell) parent);
				folderdlg.setText("输出文本路径选择");
				folderdlg.setFilterPath("SystemDrive");
				folderdlg.setMessage("请选择相应的文件夹");
				String selecteddir=folderdlg.open();
				if(selecteddir == null){
					return;
				}else{
					System.out.println("您选中的文本输出文件夹为：" + selecteddir);
				}
				String log = datePrefix + "您选中的文本输出文件夹为：" + selecteddir;
				textPathText.setText(selecteddir);	
				logDetailText.append(log + "\n");
			}
		});
		textOutputButton.setBounds(126, 107, 114, 27);
		textOutputButton.setText("输出文本目录");
		
		textPathText = new Text(container, SWT.BORDER);
		textPathText.setBounds(280, 107, 482, 25);
		
		//开始转换按钮
		Button startThansfer = new Button(container, SWT.NONE);
		startThansfer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logDetailText.append(datePrefix + "开始转换........" + "\n");
				startThansfer.setEnabled(false);
				voicePath = voicePathText.getText();
				textPath = textPathText.getText();									
				int status = 0;					
				Callable<Integer> f = new TransferThread(logDetailText, countDownLatch, datePrefix, voicePath, textPath);
				//Callable<Integer> f = new TransferThreadAsyc(parent, logDetailText, countDownLatch, datePrefix, voicePath, textPath);
//				parent.getDisplay().asyncExec(new Runnable() {					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						for(int i=0; i<20; i++){
//							try {
//								Thread.sleep(1000);
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//							logDetailText.append(datePrefix + "doing" + "\n");
//						}
//					}
//				});
				try {
					status = f.call();
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}												
				try {
					countDownLatch.await();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
				if(status == 1){
					logDetailText.append(datePrefix + "转换完成" + "\n");
				}else{
					logDetailText.append(datePrefix + "转换失败" + "\n");
				}						
				startThansfer.setEnabled(true);
			}
		});
		startThansfer.setBounds(340, 177, 283, 27);
		startThansfer.setText("开始转换");
		
		
		logDetailText = new Text(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		logDetailText.setBounds(43, 254, 805, 273);
		
		logText = new Text(container, SWT.BORDER);
		logText.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		logText.setText("日志信息");
		logText.setBounds(43, 225, 57, 23);
				
		return container;
	}
	
	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager.
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("menu");
		return menuManager;
	}

	/**
	 * Create the toolbar manager.
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Create the status line manager.
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			VoiceToText window = new VoiceToText();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText("VoiceToText");
		super.configureShell(newShell);		
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(900, 652);
	}
}
