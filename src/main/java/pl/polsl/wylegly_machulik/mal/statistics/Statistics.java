package pl.polsl.wylegly_machulik.mal.statistics;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class Statistics
{
	private final List<Long> malRecordTimes;
	private final List<Long> singleRecordTimes;
	private final List<Long> pageRecordTimes;

	public void saveSingleRecordTime(final long time)
	{
		singleRecordTimes.add(time);
	}

	public void savePageRecordTime(final long time)
	{
		pageRecordTimes.add(time);
	}

	public void saveMalRecordTime(final long time)
	{
		malRecordTimes.add(time);
	}

	public void saveStatistics(final long lastTaskTimeMillis)
	{
		try
		{
			PrintWriter writer = new PrintWriter("singleRecordTime.csv");
			StringBuilder sb = new StringBuilder();
			singleRecordTimes.forEach(singleRecordTime -> {
				sb.append(singleRecordTime);
				sb.append(",");
				sb.append("\n");
			});
			writer.write(sb.toString());
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		try
		{
			PrintWriter writer = new PrintWriter("pageRecordTime.csv");
			StringBuilder sb = new StringBuilder();
			pageRecordTimes.forEach(pageRecordTime -> {
				sb.append(pageRecordTime);
				sb.append(",");
				sb.append("\n");
			});
			writer.write(sb.toString());
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		try
		{
			PrintWriter writer = new PrintWriter("malRecordTime.csv");
			StringBuilder sb = new StringBuilder();
			malRecordTimes.forEach(malRecordTime -> {
				sb.append(malRecordTime);
				sb.append(",");
				sb.append("\n");
			});
			writer.write(sb.toString());
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		try
		{
			PrintWriter writer = new PrintWriter("allProgramTime.csv");
			StringBuilder sb = new StringBuilder();
			sb.append(lastTaskTimeMillis);
			writer.write(sb.toString());
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}
