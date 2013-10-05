package jmeter_runner.server.build_perfmon.data_providers;

import com.intellij.util.containers.SortedList;
import jetbrains.buildServer.log.Loggers;
import jmeter_runner.server.build_perfmon.graph.Graph;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public abstract class AbstractDataProvider {
	protected Map<String, Graph> metrics;
	protected File file;

	public AbstractDataProvider(@NotNull File file) {
		this.metrics = new HashMap<String, Graph>();
		this.file = file;
	}

	@NotNull
	public Collection<Graph> getData() {
		readLines();
		return getSortedGraphs();
	}

	protected void readLines() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			while (reader.ready()) {
				String[] items = reader.readLine().trim().split(",");
				if (checkItem(items)) {
					processLine(items);
				}
			}
		} catch (FileNotFoundException e) {
			Loggers.STATS.error("JMeter plugin error. File " + file.getAbsolutePath() + " not found!", e);
		} catch (IOException e) {
			Loggers.STATS.error("JMeter plugin error. Error reading file " + file.getAbsolutePath(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Loggers.STATS.error("JMeter plugin error. Error closing file " + file.getAbsolutePath(), e);
				}
			}
		}
	}

	private boolean checkItem(String[] values) {
		if (values.length < 3) {
			Loggers.STATS.error("JMeter plugin error. \nItem getMetricType: timestamp, elapsed, label, ... \n Found: " + values);
			return false;
		}
		return (values[0].matches("\\d+") && values[1].matches("\\d+"));
	}

	public abstract void processLine(String... values);

	public Collection<Graph> getSortedGraphs() {
		List<Graph> sortedMetricDescriptors = new SortedList<Graph>(new Comparator<Graph>() {
			@Override
			public int compare(Graph o1, Graph o2) {
				return o2.getOrderNumber() - o1.getOrderNumber();
			}
		});
		sortedMetricDescriptors.addAll(metrics.values());
		return sortedMetricDescriptors;
	}
}
