package it.luigibifulco.xdcc4j.downloader.core;

import it.luigibifulco.xdcc4j.search.SearchModule;

import com.google.inject.AbstractModule;

public class DownloaderServiceModule extends AbstractModule {
	private String destinationDir;

	public DownloaderServiceModule(String destinationDir) {
		this.destinationDir = destinationDir;
	}

	@Override
	protected void configure() {
		install(new SearchModule());
		bind(XdccDownloader.class).toInstance(
				new DownloaderService(destinationDir));
	}

}
