package ml.rabidbeaver.cupsprint;

import java.util.ArrayList;

public class EditControls {
	
	public final static ArrayList<Pair> orientationOpts;
	public final static ArrayList<String>protocols;
	public final static ArrayList<String>showInOpts;
	
	static{
		orientationOpts = new ArrayList<Pair>();
		orientationOpts.add(new Pair("3", "Portrait"));
		orientationOpts.add(new Pair("4", "Landscape"));
		orientationOpts.add(new Pair("5", "Reverse Portrait"));
		orientationOpts.add(new Pair("6", "Reverse Landscape"));
		
		protocols = new ArrayList<String>();
		protocols.add("http");
		protocols.add("https");
		
		showInOpts = new ArrayList<String>();
		showInOpts.add("Both");
		showInOpts.add("Shares");
		showInOpts.add("Print Service");
	}

}