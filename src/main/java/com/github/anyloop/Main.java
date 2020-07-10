package com.github.anyloop;

/**
 * The main class of the AnyLoop program.
 * 
 * @author Thomas Reiter {@link https://github.com/tom65536}
 * @since 0.1.0
 *
 */
public final class Main
{
	/**
	 * The entry point of the application.
	 * 
	 * @param args the command line arguments
	 * 
	 * @since 0.1.0
	 */
	public static final void main(String[] args)
	{
		System.out.println(getTitle() + " " + getVersion());
		System.out.println("Licensed under the EUPL");
	}
	
	public synchronized static final String getVersion()
	{
		String version = null;
		Package aPackage = Main.class.getPackage();
		if (aPackage != null) {
			version = aPackage.getImplementationVersion();
		  
			if (version == null) {
				version = aPackage.getSpecificationVersion();
			}
		}
		return version;
	}
	
	public synchronized static final String getTitle()
	{
		String title = null;
		Package aPackage = Main.class.getPackage();
		if (aPackage != null) {
			title = aPackage.getImplementationTitle();
		  
			if (title == null) {
				title = aPackage.getSpecificationTitle();
			}
		}
		return title;
	}
}
