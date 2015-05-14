package ml.rabidbeaver.cupsprint;

import java.util.ArrayList;
import java.util.List;

import ml.rabidbeaver.detect.HostScanTask;
import ml.rabidbeaver.detect.MdnsScanTask;
import ml.rabidbeaver.detect.PrinterRec;
import ml.rabidbeaver.detect.PrinterResult;
import ml.rabidbeaver.detect.PrinterUpdater;
import ml.rabidbeaver.jna.cups_dest_s;
import ml.rabidbeaver.jna.cups_option_s;
import ml.rabidbeaver.tasks.GetPrinterListener;
import ml.rabidbeaver.tasks.GetPrinterTask;
import ml.rabidbeaver.cupsjni.CupsClient;
import ml.rabidbeaver.cupsjni.JobOptions;
import ml.rabidbeaver.cupsprintservice.R;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class PrinterAddEditActivity extends Activity implements PrinterUpdater, GetPrinterListener{

	Spinner  protocol;
	EditText nickname;
	EditText host;
	EditText port;
	EditText queue;
	EditText userName;
	EditText password;
	Spinner resolution;
	Spinner orientation;
	CheckBox fitToPage;
	CheckBox fitPlot;
	CheckBox noOptions;
	CheckBox isDefault;
	String oldPrinter;
	List<JobOptions> printerOptions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.printer_add_edit_activity);
		
		Intent intent = getIntent();
		
		oldPrinter = intent.getStringExtra("printer");
		if (oldPrinter == null) oldPrinter="";
		nickname = (EditText) findViewById(R.id.editNickname);
		protocol = (Spinner) findViewById(R.id.editProtocol);
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, 
 				android.R.layout.simple_spinner_item, EditControls.protocols);
		protocol.setAdapter(aa);
		host = (EditText) findViewById(R.id.editHost);
		port = (EditText) findViewById(R.id.editPort);
		queue = (EditText) findViewById(R.id.editQueue);
		userName = (EditText) findViewById(R.id.editUserName);
		password = (EditText) findViewById(R.id.editPassword);
		orientation = (Spinner) findViewById(R.id.editOrientation);
		resolution = (Spinner) findViewById(R.id.editResolution);
		ArrayAdapter<EditControls.Pair> aa1 = new ArrayAdapter<EditControls.Pair>(this, 
	 				android.R.layout.simple_spinner_item, EditControls.orientationOpts);
	 	orientation.setAdapter(aa1);

		fitToPage = (CheckBox) findViewById(R.id.editFitToPage);
		noOptions = (CheckBox) findViewById(R.id.editNoOptions);
		isDefault = (CheckBox) findViewById(R.id.editIsDefault);
		
		Button saveBtn = (Button) findViewById(R.id.editSave);
		saveBtn.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				savePrinter(v);
			}
		});
		
		Button testBtn = (Button) findViewById(R.id.editTest);
		testBtn.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				testPrinter(v);
			}
		});

		if (!oldPrinter.contentEquals("")){
			this.setTitle(R.string.title_activity_printer_edit);

		    PrintQueueConfHandler dbconf = new PrintQueueConfHandler(getBaseContext());
		    PrintQueueConfig conf = dbconf.getPrinter(oldPrinter);
			dbconf.close();
		    if (conf != null){
		    	int size = EditControls.protocols.size();
		    	int pos = 0;
		 		for (pos=0; pos<size; pos++){
		 			String test = EditControls.protocols.get(pos);
		 			if (test.equals(conf.protocol)){
		 				protocol.setSelection(pos);
		 				break;
		 			}
		 		}
		 		nickname.setText(conf.nickname);
		    	host.setText(conf.host);
		    	port.setText(conf.port);
		    	queue.setText(conf.queue);
		    	userName.setText(conf.userName);
		    	password.setText(conf.password);
		    	size = EditControls.orientationOpts.size();
		    	pos = 0;
		 		for (pos=0; pos<size; pos++){
		 			EditControls.Pair opt = EditControls.orientationOpts.get(pos);
		 			if (opt.option.equals(conf.orientation)){
		 				orientation.setSelection(pos);
		 				break;
		 			}
		 		}
		 		fitToPage.setChecked(conf.imageFitToPage);
		 		noOptions.setChecked(conf.noOptions);
		 		isDefault.setChecked(conf.isDefault);
		 		 
		 		printerOptions = conf.printerAttributes; 
		 		List<String> resolutions = new ArrayList<String>();
				String defaultResolution = conf.resolution;
				JobOptions j;
				for (int i=0; i<printerOptions.size(); i++){
					j = printerOptions.get(i);
					if (j.name.equals("printer-resolution-supported")) resolutions.add(j.value);
				}
				ArrayAdapter<String> ab = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, resolutions);
				resolution.setAdapter(ab);
				resolution.setSelection(resolutions.indexOf(defaultResolution));
				
				findViewById(R.id.editSave).setEnabled(true);
		     }
		}
		if (oldPrinter.equals("")){
			port.setText("631");
			fitToPage.setChecked(true);
		}
			
	}

	  
   @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.printer_add_edit_menu, menu);
		return true;
	}

	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case R.id.scanhost:
	    	    String user = userName.getText().toString();
	    	    if (user.equals("")){
	    	    	user = "anonymous";
	    	    }
	    	    String passwd = password.getText().toString();
	    		new HostScanTask(this, this, user, passwd).execute();
	    		break;
	    	case R.id.scanmdns:
	    		new MdnsScanTask(this, this).execute();
	    		break;
	    }
	    return super.onContextItemSelected(item);
	 }

	private void alert(String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
		       .setTitle("error");
		AlertDialog dialog = builder.create();	
		dialog.show();
	}
	
	
	private boolean checkEmpty(String fieldName, String value){
		if (value.equals("")){
			alert(fieldName + " missing");
			return false;
		}
		return true;
	}
	
	private boolean checkInt(String fieldName, String value){
		
		try {
			@SuppressWarnings("unused")
			int test = Integer.parseInt(value);
			return true;
		}
		catch (Exception e){
			alert(fieldName + " must be an integer");
			return false;
		}
	}
	
	private boolean checkResolution(String fieldName, String resolution){
		if (resolution.equals("")){
			return true;
		}
		String[] dpis = resolution.split("x");
		if (dpis.length != 2){
			alert(fieldName + " must be empty\nOr in the format <integer>x<integer> ");
			return false;
		}
		try {
			@SuppressWarnings("unused")
			int x = Integer.parseInt(dpis[0]);
			x = Integer.parseInt(dpis[1]);
		}catch (Exception e){
			alert(fieldName + " must be empty\nOr in the format <integer>x<integer> ");
			return false;
		}
		return true;
	}
	
	private boolean checkExists(String name, PrintQueueConfHandler conf){
		
		if (oldPrinter.equals(name))
			return false;
		if (!conf.printerExists(name))
			return false;
		
		alert("Duplicate nickname: " + name);
		return true;
				
	}
	
	private void showResult(String title, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
		       .setTitle(title);
		AlertDialog dialog = builder.create();	
		try {
			dialog.show();
		}catch (Exception e){}
	}
	
	public void testPrinter(View view){
		PrintQueueConfig printConfig = new PrintQueueConfig(
				nickname.getText().toString(),
				(String)protocol.getSelectedItem(),
					host.getText().toString(),
					port.getText().toString(),
					queue.getText().toString());
		
	    CupsClient client;
	    try {
	    	client = new CupsClient(Util.getClientURL(printConfig).getHost(), Util.getClientURL(printConfig).getPort());
	    }
	    catch (Exception e){
	    	showResult("Failed", e.getMessage());
	    	return;
	    }
   
	    String user = userName.getText().toString();
	    if (user.equals("")){
	    	user = "anonymous";
	    }
	    String passwd = password.getText().toString();
	    if (!(passwd.equals(""))){
	    	client.setUserPass(user,passwd);
	    }
	    
	    GetPrinterTask task = new GetPrinterTask(client, Util.getQueue(printConfig), false);
	    task.setListener(this);
	    task.execute();
	}
	
	@Override
	public void onGetPrinterTaskDone(cups_dest_s printer, List<JobOptions> printerOptions, Exception exception) {
		this.printerOptions = printerOptions;
		
		List<String> resolutions = new ArrayList<String>();
		String defaultResolution = null;
		JobOptions j;
		for (int i=0; i<printerOptions.size(); i++){
			j = printerOptions.get(i);
			if (j.name.equals("printer-resolution-supported")) resolutions.add(j.value);
			if (j.name.equals("printer-resolution-default")) defaultResolution = j.value;
		}
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, resolutions);
		resolution.setAdapter(aa);
		resolution.setSelection(resolutions.indexOf(defaultResolution));
		
		findViewById(R.id.editSave).setEnabled(true);
	    
		if (exception != null){
	    	showResult("Failed", exception.getMessage());
	    	return;
	    }
	    
	    if (printer == null){
	    	showResult("Failed", "Printer not found");
	    	return;
	    }
	    
	    cups_option_s opts = printer.options;
	    opts.read();
	    cups_option_s[] optsarr = (cups_option_s[]) opts.toArray(printer.num_options);
	    
	    String result = "queue: " + printer.name.getString(0);
	    for (int i=0; i<printer.num_options; i++){
	    	result += "\n"+ optsarr[i].name.getString(0) + ": " + optsarr[i].value.getString(0);
	    }
			    
		showResult("Success", result);
	}
	
	public void savePrinter(View view) {
	     final PrintQueueConfHandler confdb = new PrintQueueConfHandler(getBaseContext());
	     String sNickname = nickname.getText().toString().trim();
	     if (!checkEmpty("Nickname", sNickname)){
	    	 nickname.requestFocus();
	    	 return;
	     }
	     if (checkExists(sNickname, confdb)){
	    	 nickname.requestFocus();
	    	 return;
	     }
	     String sHost = host.getText().toString().trim();
	     if (!checkEmpty("Host", sHost)){
	    	 host.requestFocus();
	    	 return;
	     }
	     String sPort = port.getText().toString().trim();
	     if (!checkEmpty("Port", sPort)){
	    	 port.requestFocus();
	    	 return;
	     }
	     if (!checkInt("Port", sPort)){
	    	 port.requestFocus();
	    	 return;
	     }
	     String sQueue = queue.getText().toString().trim();
	     if (!checkEmpty("Queue", sQueue)){
	    	 queue.requestFocus();
	    	 return;
	     }
	     String sUserName = userName.getText().toString().trim();
	     if (sUserName.equals("")){
	    	 sUserName = "anonymous";
	     }
	     String sProtocol = (String) protocol.getSelectedItem();
	     String sPassword = password.getText().toString().trim();
	     //String sResolution = resolution.getText().toString().trim().toLowerCase(Locale.US);
	     String sResolution = (String)resolution.getSelectedItem();
	     if (!checkResolution("Resolution", sResolution)){
	    	 resolution.requestFocus();
	    	 return;
	     }
	     final PrintQueueConfig conf = new PrintQueueConfig(sNickname, sProtocol, sHost, sPort, sQueue);
	     if (checkExists(conf.getPrintQueue(), confdb)){
	    	 host.requestFocus();
	    	 return;
	     }
	     conf.userName = sUserName;
	     conf.password = sPassword;
	     EditControls.Pair opt = (EditControls.Pair) orientation.getSelectedItem();
	     conf.orientation = opt.option;
	     conf.extensions = "";
	     conf.imageFitToPage = fitToPage.isChecked();
	     conf.noOptions = noOptions.isChecked();
	     conf.isDefault = isDefault.isChecked();
	     conf.resolution = sResolution;
	     conf.printerAttributes = printerOptions;
	     if ((conf.protocol.equals("http")) && (!(conf.password.equals("")))){
	         AlertDialog.Builder builder = new AlertDialog.Builder(this);
	         builder.setTitle("Warning: Using password with http protocol")
	         		.setMessage("This will result in both your username and password being sent over the network as plain text. "
	         				+ "Using the https protocol is reccomended for authentication.")
	                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int id) {
	                    	doSave(confdb, conf);
	                    }
	                })
	                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int id) {
	                    }
	                });
	         builder.create().show();
	    	 return;
	     }
	     doSave(confdb, conf);
	}
	     
	public void doSave(PrintQueueConfHandler confdb, PrintQueueConfig conf){
	     confdb.addOrUpdatePrinter(conf, oldPrinter);
	     CupsPrintApp.getPrinterDiscovery().updateStaticConfig();
		 Intent intent = new Intent(this, PrinterMainActivity.class);
	     startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}
	
	public void getDetectedPrinter(PrinterResult results){
			
			//List<String> errors = results.getErrors();
			final List<PrinterRec> printers = results.getPrinters();
			/*if (errors.size() > 0){
				String errorMessage = "";
				for (String error: errors){
					errorMessage = errorMessage + error + "\n";
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Scan messages");
				builder.setMessage(errorMessage);
				builder.setOnCancelListener(new OnCancelListener() {
				    public void onCancel(final DialogInterface dialog) {
				    	chooseDetectedPrinter(printers);
				    }
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
			else {*/
				chooseDetectedPrinter(printers);
			//}
		}
			
	public void chooseDetectedPrinter(List<PrinterRec> printers){
		if (printers.size() < 1){
			showResult("", "No printers found");
			return;
		}
		final ArrayAdapter<PrinterRec> aa = new ArrayAdapter<PrinterRec>(
				this, android.R.layout.simple_list_item_1,printers); 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select printer");
		builder.setAdapter(aa, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PrinterRec printer = aa.getItem(which);
				int size = protocol.getCount();
				int i;
				for (i=0; i<size; i++){
					if (protocol.getItemAtPosition(i).equals(printer.getProtocol())){
						protocol.setSelection(i);
						break;
					}
				}
				nickname.setText(printer.getNickname());
				protocol.setSelection(i);
				host.setText(printer.getHost());
				port.setText(String.valueOf(printer.getPort()));
				queue.setText(printer.getQueue());					
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private static class EditControls {
		
		public final static ArrayList<Pair> orientationOpts;
		public final static ArrayList<String>protocols;
		
		static {
			orientationOpts = new ArrayList<Pair>();
			orientationOpts.add(new Pair("3", "Portrait"));
			orientationOpts.add(new Pair("4", "Landscape"));
			orientationOpts.add(new Pair("5", "Reverse Portrait"));
			orientationOpts.add(new Pair("6", "Reverse Landscape"));
			
			protocols = new ArrayList<String>();
			protocols.add("http");
			protocols.add("https");
		}
		private static class Pair {
			String option;
			String name;
			public Pair(String option, String name){
				this.option = option;
				this.name = name;
			}
			public String toString(){
				return name;
			}
		}
	}

}
