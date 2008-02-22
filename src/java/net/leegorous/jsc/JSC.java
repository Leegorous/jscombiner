package net.leegorous.jsc;

public class JSC {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length==0 || args.length%2==1) {
			System.out.println("Usage: JSA -path configPath");
		} else {
			for (int i = 0; i < args.length; i+=2) {
				if (args[i].equals("-path")) {
					System.out.println("Set configurate path: "+args[i+1]);
					try {
						JavaScriptCombiner jsa = new JavaScriptCombiner(args[i+1]);
						jsa.assemble();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("Done.");
	}

}
