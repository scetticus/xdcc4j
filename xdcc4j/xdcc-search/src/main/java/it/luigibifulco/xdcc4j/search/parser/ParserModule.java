package it.luigibifulco.xdcc4j.search.parser;

import it.luigibifulco.xdcc4j.search.parser.annotations.CmPlus;
import it.luigibifulco.xdcc4j.search.parser.annotations.Puffolandia;
import it.luigibifulco.xdcc4j.search.parser.annotations.XdccFinder;
import it.luigibifulco.xdcc4j.search.parser.annotations.XdccIt;

import com.google.inject.AbstractModule;

/**
 * 
 * @author Luigi
 *
 */
public class ParserModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(XdccHtmlParser.class).annotatedWith(XdccIt.class).to(
				XdccItParser.class);
		bind(XdccHtmlParser.class).annotatedWith(XdccFinder.class).to(
				XdccFinderParser.class);
		bind(XdccHtmlParser.class).annotatedWith(CmPlus.class).to(
				CmPlusParser.class);
		bind(XdccHtmlParser.class).annotatedWith(Puffolandia.class).to(
				PuffolandiaParser.class);

		// bind(XdccHtmlParser.class).annotatedWith(Names.named("xdccfinder")).to(
		// XdccFinderParser.class);
	}
}
