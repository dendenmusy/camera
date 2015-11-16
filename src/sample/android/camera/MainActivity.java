package sample.android.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ZoomControls;

public class MainActivity extends Activity implements SensorEventListener {
	// カメラインスタンス
	private Camera camera = null;

	// カメラパラメータ取得
	//public Parameters parameters = camera.getParameters();

	// カメラプレビュークラス
	//private CameraPreview cameraPreview = null;

	// サーフィスビュー(カメラのプレビューをここに表示)
	private SurfaceView surfaceView;

	// 画面タッチの2度押し禁止用フラグ
    private boolean mIsTake = false;

    // センサーマネージャー
    private SensorManager sensorManager;

    // テキスト
    //private TextView values;

    // 画像
    //private ImageView imgView;

    //private static final int REQUEST_GALLERY = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//SurfaceView
        surfaceView = (SurfaceView)findViewById(R.id.mySurfaceView);

        // サーフェスホルダーの取得
        SurfaceHolder holder = surfaceView.getHolder();
        // コールバック通知先の設定
        holder.addCallback(callback);
        //holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

//		// カメラインスタンスの取得
//        try {
//            // カメラ起動
//        	camera = Camera.open();
//        } catch (Exception e) {
//            // エラー
//            this.finish();
//        }


        // FrameLayout に CameraPreview クラスを設定
//        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
//        cameraPreview = new CameraPreview(this, camera);
//        preview.addView(cameraPreview);

        // cameraPreview に タッチイベントを設定
        surfaceView.setOnTouchListener(new OnTouchListener() {
           public boolean onTouch(View v, MotionEvent event) {
                if (mIsTake) {
                	return true;
                }

            	if (event.getAction() == MotionEvent.ACTION_DOWN) {

                        // 撮影中の2度押し禁止用フラグ
                        // mIsTake = true;
                        // 画像取得
                        camera.autoFocus(mAutoFocusListener);

                }
                return true;
            }
        });

        /**
         * 撮影ボタン
         */
        // 撮影ボタンの取得
        ImageButton imgButton = (ImageButton)findViewById(R.id.imageButton1);
        imgButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				camera.takePicture(null, null, mPicJpgListener);
			}

        });

        // テキストビュー(拡大率)の取得
        final TextView textView = (TextView)findViewById(R.id.TextView01);
        /**
         *  ズームコントロール
         */
        // ズームコントロールの取得
        ZoomControls zoomCtr = (ZoomControls)findViewById(R.id.zoomControls1);
        // ズームイン
        zoomCtr.setOnZoomInClickListener(new OnClickListener() {
            public void onClick(View v) {
                Camera.Parameters prm = camera.getParameters();
                // 現在のズーム値の取得
                int nowZoom = prm.getZoom();
                if (nowZoom < prm.getMaxZoom()) {
                	prm.setZoom(nowZoom + 1);
                	textView.setText("拡大率:"+ (nowZoom + 1));
                }
                // パラメータ設定
                camera.setParameters(prm);
            }
        });
        // ズームアウト
        zoomCtr.setOnZoomOutClickListener(new OnClickListener() {
            public void onClick(View v) {
                Camera.Parameters prm = camera.getParameters();
                // 現在のズーム値の取得
                int nowZoom = prm.getZoom();
                if (nowZoom > 0) {
                    prm.setZoom(nowZoom - 1);
                    textView.setText("拡大率:"+ (nowZoom - 1));
                }
            	// パラメータ設定
                camera.setParameters(prm);
            }
        });

        /**
         * 拡大率設定シークバー
         */
        final SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar1);

        // シークバーの初期値をTextViewに表示
        textView.setText("拡大率:" + seekBar.getProgress());

        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        			public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser) {
        				// ツマミをドラッグしたときに呼ばれる
        				Camera.Parameters parameters = camera.getParameters();
        				int nowZoom = progress;
        				if (nowZoom < parameters.getMaxZoom()) {
        					textView.setText("拡大率:"+ nowZoom);
        					// パラメータ設定
        					parameters.setZoom(nowZoom);
        					camera.setParameters(parameters);
        				}
        			}

        			public void onStartTrackingTouch(SeekBar seekBar) {
        				// ツマミに触れたときに呼ばれる
        			}

        			public void onStopTrackingTouch(SeekBar seekBar) {
        				// ツマミを離したときに呼ばれる
        			}
        		}
        		);

    	/**
    	 * フラッシュ
    	 */
    	// チェックボックス(初期状態はfalse)
    	final CheckBox checkBox = (CheckBox)findViewById(R.id.checkBox1);
    	//checkBox.setChecked(true);
    	checkBox.setText("フラッシュ");
    	checkBox.setOnClickListener(new OnClickListener() {
    		@Override
			public void onClick(View view) {
    			Camera.Parameters parameters = camera.getParameters();
    			final boolean isChecked = ((CheckBox)view).isChecked();
    			if (isChecked) {
    				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
    		    	camera.setParameters(parameters);
    			} else {
    				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
    		    	camera.setParameters(parameters);
    		    }
			}
    	}
    	);

    	/**
    	 * ホワイトバランス
    	 */
		// ラジオボタン
    	// ラジオグループオブジェクトを取得
    	RadioGroup radioGroup = (RadioGroup)findViewById(R.id.RadioGroup);
    	// ラジオボタンオブジェクトを取得
    	final RadioButton radioButton1 = (RadioButton)findViewById(R.id.RadioButton1);
		final RadioButton radioButton2 = (RadioButton)findViewById(R.id.RadioButton2);
		final RadioButton radioButton3 = (RadioButton)findViewById(R.id.RadioButton3);
		radioButton1.setText("WB:自動");
		radioButton2.setText("WB:昼");
		radioButton3.setText("WB:くもり");
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			// チェック状態変更時に呼び出されるメソッド
    		public void onCheckedChanged(RadioGroup group, int checkedId) {
    			Camera.Parameters parameters = camera.getParameters();
    			if (radioButton1.isChecked() == true) {
    				parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
    		    	camera.setParameters(parameters);
    			} else if (radioButton2.isChecked() == true) {
    				parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
    		    	camera.setParameters(parameters);
    			} else if (radioButton3.isChecked() == true) {
    				parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
    		    	camera.setParameters(parameters);
    			}
    		}
    	});


		// センサーマネージャ
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);


	}// ▲OnCreate()の終わり▲

//		imgView = (ImageView)findViewById(R.id.imageView1);
//		// ギャラリー呼び出し
//		Intent intent = new Intent();
//		intent.setType("image/*");
//		intent.setAction(Intent.ACTION_GET_CONTENT);
//		startActivityForResult(intent, REQUEST_GALLERY);

    	/**
    	 * 照度センサー
    	 */
//    	super.onCreate(savedInstanceState);
//    	setContentView(R.layout.activity_main);
//
//    	values = (TextView)findViewById(R.id.textView1);
//    	sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);



    /**
     * 撮影した画像の表示枠
     */
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//	 // TODO Auto-generated method stub
//		if(requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
//			try {
//				InputStream in = getContentResolver().openInputStream(data.getData());
//				Bitmap img = BitmapFactory.decodeStream(in);
//				in.close();
//				// 選択した画像を表示
//				imgView.setImageBitmap(img);
//			} catch (Exception e) {
//
//			}
//		}
//	}


	/**
	 * オートフォーカス完了のコールバック
	 */
	private Camera.AutoFocusCallback mAutoFocusListener = new Camera.AutoFocusCallback() {
	    public void onAutoFocus(boolean success, Camera camera) {
	        // 撮影
	        //camera.takePicture(null, null, mPicJpgListener);
	    }
	};

//	@Override
//    protected void onPause() {
//		//camera.stopPreview();
//        // カメラ破棄インスタンスを解放
//        if (this.camera != null) {
//            this.camera.release();
//            this.camera = null;
//        }
//        super.onPause();
//    }

	// ホームボタンを押下すると呼び出される
	// 戻るボタンを押下すると呼び出されない
	// 【ホームボタンを押下し、カメラを開放したい！】
//	@Override
//	protected void onRestart() {
//		//this.finish();
//		//camera = Camera.open();
//	}
//
//	// ホームボタンを押下すると呼び出されない
//	// 戻るボタンを押下すると呼び出される
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//		camera.release();
//	}

	 /**
     * JPEG データ生成完了時のコールバック
     */
    private Camera.PictureCallback mPicJpgListener = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

        	if (data == null) {
                return;
            }

            String saveDir = Environment.getExternalStorageDirectory().getPath() + "/test";

            // SD カードフォルダを取得
            File file = new File(saveDir);

            // フォルダ作成
            if (!file.exists()) {
                if (!file.mkdir()) {
                    Log.e("Debug", "Make Dir Error");
                }
            }

            // 画像保存パス
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String imgPath = saveDir + "/" + sf.format(cal.getTime()) + ".jpg";

            // ファイル保存
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(imgPath, true);
                fos.write(data);
                fos.close();

                // アンドロイドのデータベースへ登録
                // (登録しないとギャラリーなどにすぐに反映されないため)
                registAndroidDB(imgPath);

            } catch (Exception e) {
                Log.e("Debug", e.getMessage());
            }

            fos = null;

            // takePicture するとプレビューが停止するので、再度プレビュースタート
            camera.startPreview();

            mIsTake = false;
        }
    };



    /**
     * アンドロイドのデータベースへ画像のパスを登録
     * @param path 登録するパス
     */
    private void registAndroidDB(String path) {
        // アンドロイドのデータベースへ登録
        // (登録しないとギャラリーなどにすぐに反映されないため)
        ContentValues values = new ContentValues();
        ContentResolver contentResolver = MainActivity.this.getContentResolver();
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put("_data", path);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }



 	/**
 	 * メニュー (未定)
 	 */
    /*
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
*/
/*	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
*/
	//コールバック
    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {

		@Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {

            // ▼カメラインスタンス取得▼
            camera = Camera.open();

            // 出力をSurfaceViewに設定
            try{
                camera.setPreviewDisplay(surfaceHolder);
            }catch(Exception e){
                //e.printStackTrace();
            }

            /**
             * 解像度設定
             */
        	// サポートピクチャサイズ取得
            Camera.Parameters parameters = camera.getParameters();
            List< Size > sizeList = parameters.getSupportedPictureSizes();
        	for (int i=0; i < sizeList.size(); i++) {
        		Log.v("CameraPictureSize", "Size = " + sizeList.get(i).width + "x" + sizeList.get(i).height);
        	}
        	// サイズ：MaxSize(3264x2448)に設定
        	parameters.setPictureSize(sizeList.get(0).width, sizeList.get(0).height);
        	// パラメータ設定
        	camera.setParameters(parameters);


        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        	// 起動時にチェックボックスのチェックを外す
        	final CheckBox checkBox = (CheckBox)findViewById(R.id.checkBox1);
        	checkBox.setChecked(false);
        	// 起動時にラジオボタン１をチェックする
        	final RadioButton radioButton1 = (RadioButton)findViewById(R.id.RadioButton1);
    		radioButton1.setChecked(true);

        	// プレビューを止める
    		camera.stopPreview();

            // カメラの情報を取り出す
            Parameters parameters = camera.getParameters();

            int maxWidth = 0;
            int prevWidth = width;
            int prevHeight = height;

            // カメラがサポートしているプレビューイメージのサイズ
            List<Size> sizeList = parameters.getSupportedPreviewSizes();
            //parameters.setPreviewSize(sizeList.get(0).width, sizeList.get(0).height);

            // サポートするサイズを一通りチェック
            for (Size s : sizeList) {

            	// プレビューするサーフィスサイズより大きいものは無視
            	// 画面に収まるサイズを選択するため
            	if ((prevWidth < s.width) || (prevHeight < s.height)) {
            		continue;
            	}

            	// プレビューサイズの中で一番大きいものを選ぶ
            	if (maxWidth < s.width) {
            		maxWidth = s.width;
            		prevWidth = s.width;
            		prevHeight = s.height;
            	}
            }

            // プレビューサイズをカメラのパラメータにセットする
            // 得られたプレビューサイズは画面に収まる
            parameters.setPreviewSize(prevWidth, prevHeight);

            // 実際のプレビュー画面への拡大率を設定する
            float wScale = width / prevWidth;
            float hScale = height / prevHeight;

            // 画面内に収まらないといけないから拡大率は幅と高さで小さい方を採用する
            float prevScale = wScale < hScale ? wScale : hScale;
            // SurfaceViewのサイズをセットする
            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();

            layoutParams.width = (int)(prevWidth * prevScale);
            layoutParams.height = (int)(prevHeight * prevScale);
//            final TextView textView = (TextView)findViewById(R.id.TextView01);
//            textView.setText(layoutParams.width + ":" + layoutParams.height);

            // レイアウトのサイズを設定し直して画像サイズに一致するようにする
            // 一致させないと画像がのびる; layoutParams( width : height = 1280 : 720 )
            surfaceView.setLayoutParams(layoutParams);

            // カメラにプレビューの設定情報を戻す
            camera.setParameters(parameters);

        	try {
                // カメラインスタンスに、画像表示先を設定
                camera.setPreviewDisplay(surfaceHolder);
                // プレビュー開始
                camera.startPreview();
            } catch (IOException e) {
                //
            }

        }

		@Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    		// ▼カメラ解放▼
        	if (camera != null) {
        		camera.stopPreview();
        		camera.release();
        		camera = null;
        	}
        }
    };

    @Override
    protected void onStop() {
    	super.onStop();
    	// Listenerの登録解除
    	sensorManager.unregisterListener(this);
    	//final RadioButton radioButton1 = (RadioButton)findViewById(R.id.RadioButton1);

    }

    @Override
    protected void onResume() {
    	super.onResume();
    	// Listenerの登録
    	List<Sensor> sensors1 = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
    	if (sensors1.size() > 0) {
    		Sensor s = sensors1.get(0);
    		sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
    	}
    	List<Sensor> sensors2 = sensorManager.getSensorList(Sensor.TYPE_LIGHT);
    	if (sensors2.size() > 0) {
    		Sensor s = sensors2.get(0);
    		sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
    	}
    }

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO 自動生成されたメソッド・スタブ
		// TYPE_ORIENTATION ⇒ 傾きセンサー
		if(event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			String s =
					"方位角:" + event.values[0]
							+ "\n傾斜角:" + event.values[1]
									+ "\n回転角:" + event.values[2];
			// テキストビュー(傾きセンサー)の取得
			final TextView textView1 = (TextView)findViewById(R.id.Sensor1);
			textView1.setText(s);

			if (event.values[1] <= 1 && event.values[1] >= -1 || event.values[1] >= 179 || event.values[1] <= -179) {
				textView1.setTextColor(0xFFff0000);
			} else {
				textView1.setTextColor(0xFFffffff);
			}
		}
		// TYPE_LIGHT ⇒ 照度センサー
//		if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
//			String s =
//					"照度:" + event.values[0];
//			// テキストビュー(照度センサー)の取得
//			final TextView textView2 = (TextView)findViewById(R.id.Sensor2);
//			textView2.setText(s);
//		}
    	// 時間
    	TextView dateText = (TextView)findViewById(R.id.time);
    	Time time = new Time("Asia/Tokyo");
    	time.setToNow(); // 現在時刻
    	String date = time.year + "年" + (time.month+1) + "月" + time.monthDay + "日\n"
    			+ time.hour + "時" + time.minute + "分" + time.second + "秒";
    	dateText.setText(date);
	}



	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO 自動生成されたメソッド・スタブ

	}
}
