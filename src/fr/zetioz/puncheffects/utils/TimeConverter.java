package fr.zetioz.puncheffects.utils;

public class TimeConverter {
	
	private TimeConverter()
	{
		throw new IllegalStateException("Time Converter Class (Utility Class)");
	}
	
	public static long stringTimeToMillis(String timeString)
	{
		char timeChar = timeString.charAt(timeString.length() - 1);
		if(timeChar == 's')
		{
			String seconds = timeString.substring(0, timeString.length() - 1);
			if(Integer.valueOf(seconds) instanceof Integer)
			{
				return (long) Integer.valueOf(seconds) * 1000;
			}
		}
		else if(timeChar == 'm')
		{
			String minutes = timeString.substring(0, timeString.length() - 1);
			if(Integer.valueOf(minutes) instanceof Integer)
			{
				return (long) Integer.valueOf(minutes) * 60 * 1000;
			}
		}
		else if(timeChar == 'h')
		{
			String hours = timeString.substring(0, timeString.length() - 1);
			if(Integer.valueOf(hours) instanceof Integer)
			{
				return (long) Integer.valueOf(hours) * 3600 * 1000;
			}
		}
		else if(timeChar == 'd')
		{
			String days = timeString.substring(0, timeString.length() - 1);
			if(Integer.valueOf(days) instanceof Integer)
			{
				return (long) Integer.valueOf(days) * 3600 * 24 * 1000;
			}
		}
		return 0;
	}
}
