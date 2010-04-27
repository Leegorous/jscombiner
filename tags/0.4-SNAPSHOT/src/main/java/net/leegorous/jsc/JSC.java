/*
 * Copyright 2008 leegorous.net
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
