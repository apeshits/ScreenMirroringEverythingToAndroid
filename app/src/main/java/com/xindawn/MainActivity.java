package com.xindawn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.xindawn.center.DMRCenter;
import com.xindawn.center.MediaRenderProxy;
import com.xindawn.datastore.LocalConfigSharePreference;
import com.xindawn.util.CommonLog;
import com.xindawn.util.DlnaUtils;
import com.xindawn.util.LogFactory;


/**
 * @author lance
 * @csdn  http://blog.csdn.net/geniuseoe2012
 * @github https://github.com.xindawn
 */
public class MainActivity extends BaseActivity implements OnClickListener, DeviceUpdateBrocastFactory.IDevUpdateListener{

private static final CommonLog log = LogFactory.createLog();
	
	private Button mBtnStart;
	private Button mBtnReset;
	private Button mBtnStop;
	
	private Button mBtnEditName;
	private EditText mETName;
	private TextView mTVDevInfo;
	private TextView mVersion;

	private CheckBox mCkAuto;          //用于显示选项
	private CheckBox mCkFullscreen;
	private CheckBox mCkForceMirroring;

	private MediaRenderProxy mRenderProxy;
	private RenderApplication mApplication;
	private DeviceUpdateBrocastFactory mBrocastFactory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		log.setTag("MainActivity");
		
		setupView();
		initData();
	}


	//public boolean onKeyDown(int keyCode, KeyEvent event);
	/*public boolean onKeyUp(int keyCode, KeyEvent event){
		if (keyCode == KeyEvent.KEYCODE_BACK){
			finish();
			return true;
		}
		return super.onKeyUp(keyCode,event);
	}*/
	
	@Override
	protected void onDestroy() {
		unInitData();	
		super.onDestroy();
	}




	private void setupView(){
		mBtnStart = (Button) findViewById(R.id.btn_init);
    	mBtnReset = (Button) findViewById(R.id.btn_reset);
    	mBtnStop = (Button) findViewById(R.id.btn_exit);
    	mBtnEditName = (Button) findViewById(R.id.bt_dev_name);
    	mBtnStart.setOnClickListener(this);
    	mBtnReset.setOnClickListener(this);
    	mBtnStop.setOnClickListener(this);
    	mBtnEditName.setOnClickListener(this);
    	
    	mTVDevInfo = (TextView) findViewById(R.id.tv_dev_info);
		mVersion = findViewById(R.id.version_info_v);
    	mETName = (EditText) findViewById(R.id.et_dev_name);


    	mCkAuto = (CheckBox) findViewById(R.id.checkbox1);
    	mCkAuto.setOnClickListener(this);
    	mCkFullscreen = (CheckBox) findViewById(R.id.checkbox2);
    	mCkFullscreen.setOnClickListener(this);
		mCkForceMirroring = (CheckBox) findViewById(R.id.checkbox3);
		mCkForceMirroring.setOnClickListener(this);
	}

	private void initData(){
		mApplication = RenderApplication.getInstance();
		mRenderProxy = MediaRenderProxy.getInstance();
		mBrocastFactory = new DeviceUpdateBrocastFactory(this);

		String dev_name = DlnaUtils.getDevName(this);
		mETName.setText(dev_name);
		mETName.setEnabled(false);
		
		updateDevInfo(mApplication.getDevInfo());
		mBrocastFactory.register(this);

        //think about "autoBoot"
		//start(); or reset();
        reset();
	}

	private void unInitData(){
		stop();

		mBrocastFactory.unregister();
	}

	private void updateDevInfo(DeviceInfo object){
		String status = object.status ? "open" : "close";
		String text = RenderApplication.getInstance().getVersionName()+"."+RenderApplication.getInstance().getVersionCode();
		mTVDevInfo.setText(object.dev_name);
		mVersion.setText(text);

		//fuck : this doesn't work ,MUST BE RenderApplication.getInstance()
		log.d("updateDevInfo:"+LocalConfigSharePreference.getSettingsVal(RenderApplication.getInstance(),"autoBoot")+" "+LocalConfigSharePreference.getSettingsVal(RenderApplication.getInstance(),"forceFullScreen"));
		//mCkAuto.setChecked(LocalConfigSharePreference.getSettingsVal(this,"autoBoot")=="true" ? true : false);
		//mCkFullscreen.setChecked(LocalConfigSharePreference.getSettingsVal(this,"forceFullScreen")=="true" ? true : false);

		mCkAuto.setChecked(LocalConfigSharePreference.getSettingsVal(RenderApplication.getInstance(),"autoBoot").equals("true") ? true : false);
		mCkFullscreen.setChecked(LocalConfigSharePreference.getSettingsVal(RenderApplication.getInstance(),"forceFullScreen").equals("true") ? true : false);
		mCkForceMirroring.setChecked(LocalConfigSharePreference.getSettingsVal(RenderApplication.getInstance(),"forceMirroring").equals("true") ? true : false);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_init:
			start();
			break;
		case R.id.btn_reset:
			reset();
			break;
		case R.id.btn_exit:
			stop();
			//finish();
			break;
		case R.id.bt_dev_name:
		case R.id.checkbox1:
		case R.id.checkbox2:
		case R.id.checkbox3:
			change();
			break;
		}
	}

	private void start(){
		DMRCenter.killStaticInstance();
		mRenderProxy.startEngine();
	}
	
	private void reset(){
		DMRCenter.killStaticInstance();
		mRenderProxy.restartEngine();
	}
	
	private void stop(){
		mRenderProxy.stopEngine();
	}
	
	private void change(){
		if (mETName.isEnabled()){
			mETName.setEnabled(false);
			DlnaUtils.setDevName(this, mETName.getText().toString());
		}else{
			mETName.setEnabled(true);
		}

		if(mCkAuto.isChecked())
		{
			//LocalConfigSharePreference.commitSettingsVal(this,"autoBoot","true");
			LocalConfigSharePreference.commitSettingsVal(RenderApplication.getInstance(),"autoBoot","true");
			log.d("auto start 1");
		}
		else{
			//LocalConfigSharePreference.commitSettingsVal(this,"autoBoot","false");
			LocalConfigSharePreference.commitSettingsVal(RenderApplication.getInstance(),"autoBoot","false");
			log.d("auto start 0");
		}

		if(mCkFullscreen.isChecked())
		{
			//LocalConfigSharePreference.commitSettingsVal(this,"forceFullScreen","true");
			LocalConfigSharePreference.commitSettingsVal(RenderApplication.getInstance(),"forceFullScreen","true");
			log.d("fullscreen  1");
		}
		else{
			//LocalConfigSharePreference.commitSettingsVal(this,"forceFullScreen","false");
			LocalConfigSharePreference.commitSettingsVal(RenderApplication.getInstance(),"forceFullScreen","false");

			log.d("fullscreen 0");
		}

		if(mCkForceMirroring.isChecked())
		{
			//LocalConfigSharePreference.commitSettingsVal(this,"forceFullScreen","true");
			LocalConfigSharePreference.commitSettingsVal(RenderApplication.getInstance(),"forceMirroring","true");
			log.d("force mirroring  1");
		}
		else{
			//LocalConfigSharePreference.commitSettingsVal(this,"forceFullScreen","false");
			LocalConfigSharePreference.commitSettingsVal(RenderApplication.getInstance(),"forceMirroring","false");

			log.d("force mirroring 0");
		}
	}


	@Override
	public void onUpdate(Intent intent) {
		String playCommand = intent.getStringExtra("command");

		if (null != playCommand) {
			log.d("onUpdate ignore "+playCommand);
		}else {
			updateDevInfo(mApplication.getDevInfo());
		}
	}

}
