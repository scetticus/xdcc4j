package it.luigibifulco.xdcc4j.ui.client.search.controller;

import it.luigibifulco.xdcc4j.common.model.DownloadBean;
import it.luigibifulco.xdcc4j.ui.client.Registry;
import it.luigibifulco.xdcc4j.ui.client.search.event.DownloadRequestHandler;
import it.luigibifulco.xdcc4j.ui.client.search.event.XdccWebSocket;
import it.luigibifulco.xdcc4j.ui.client.search.view.HeaderUi;
import it.luigibifulco.xdcc4j.ui.client.search.view.SearchUI;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public class DownloadRequestController implements DownloadRequestHandler {

	@Override
	public void onDownloadCancel(final DownloadBean download) {
		final HeaderUi header = Registry.get("header");
		RequestBuilder cancelRequest = new RequestBuilder(RequestBuilder.GET,
				"services/downloader/cancelDownload?id=" + download.getId());
		cancelRequest.setCallback(new RequestCallback() {

			@Override
			public void onResponseReceived(Request request, Response response) {

				String sRsp = response.getText();
				if (sRsp != null && !sRsp.isEmpty()) {
					header.info("Il Download di " + download.getDesc() + "da"
							+ download.getFrom() + "@" + download.getChannel()
							+ " e' stato annullato!");
				} else {
					header.info("Il Download di " + download.getDesc() + "da"
							+ download.getFrom() + "@" + download.getChannel()
							+ " NON e' stato annullato!");
				}

			}

			@Override
			public void onError(Request request, Throwable exception) {
				header.info("Il Download di " + download.getDesc() + "da"
						+ download.getFrom() + "@" + download.getChannel()
						+ " NON e' stato annullato: " + exception.getMessage());

			}
		});
		try {
			cancelRequest.send();
		} catch (RequestException e) {
			header.info("C'� un disturbo nella forza: " + e.getMessage());
		}
	}

	@Override
	public void onDownloadRequest(final DownloadBean download) {

		RequestBuilder connectedRequest = new RequestBuilder(
				RequestBuilder.GET, "services/downloader/isConnected");
		final RequestBuilder startDownloadRequest = new RequestBuilder(
				RequestBuilder.GET, "services/downloader/startDownload?id="
						+ download.getId());
		connectedRequest.setCallback(new RequestCallback() {

			@Override
			public void onResponseReceived(Request request, Response response) {
				String sRsp = response.getText();
				if (sRsp != null && !sRsp.isEmpty()) {
					startDownloadRequest.setCallback(new RequestCallback() {

						@Override
						public void onResponseReceived(Request request,
								Response response) {
							XdccWebSocket socket = Registry.get("websocket");
							socket.subscribeDownloadStatus(download);
							HeaderUi header = Registry.get("header");
							header.info("Download di "
									+ download.getDesc()
									+ " avviato. Consulta la pagina dei downloads per monitorare lo stato dei tuoi downloads.");

						}

						@Override
						public void onError(Request request, Throwable exception) {
							HeaderUi header = Registry.get("header");
							header.info("Non riesco ad avviare il download di "
									+ download.getDesc());

						}
					});
					try {
						startDownloadRequest.send();
					} catch (RequestException e) {
						HeaderUi header = Registry.get("header");
						header.info("C'� un disturbo nella forza: "
								+ e.getMessage());
					}
				} else {

					// no server forced try to using download reques server
					String server = download.getServer();
					if (server == null || server.isEmpty()) {
						HeaderUi header = Registry.get("header");
						header.info("Prova a connetterti prima ad un server, successivamente riprova ad avviare il download");
						return;
					}
					RequestBuilder setServerReq = new RequestBuilder(
							RequestBuilder.GET,
							"services/downloader/setServer?server=" + server);
					setServerReq.setCallback(new RequestCallback() {

						@Override
						public void onResponseReceived(Request request,
								Response response) {
							try {
								startDownloadRequest.send();
							} catch (RequestException e) {
								HeaderUi header = Registry.get("header");
								header.info("C'� un disturbo nella forza: "
										+ e.getMessage());
							}

						}

						@Override
						public void onError(Request request, Throwable e) {
							HeaderUi header = Registry.get("header");
							header.info("C'� un disturbo nella forza: "
									+ e.getMessage());

						}
					});

				}

			}

			@Override
			public void onError(Request request, Throwable exception) {
				HeaderUi header = Registry.get("header");
				header.info("Non riesco a capire se sei connesso ad un server");

			}
		});
		try {
			connectedRequest.send();
		} catch (RequestException e) {
			HeaderUi header = Registry.get("header");
			header.info("C'� un disturbo nella forza: " + e.getMessage());
		}
	}

	@Override
	public void onDownloadRemove(final DownloadBean download) {
		final HeaderUi header = Registry.get("header");
		RequestBuilder removeRequest = new RequestBuilder(RequestBuilder.GET,
				"services/downloader/removeDownload?id=" + download.getId());
		removeRequest.setCallback(new RequestCallback() {
			@Override
			public void onError(Request request, Throwable exception) {
				header.info("Non riesco a rimouvere questo download: "
						+ download.getDesc());

			}

			@Override
			public void onResponseReceived(Request request, Response response) {
				if (response.getText().contains("true")) {
					header.info("Downdload Rimosso: " + download.getDesc());
				}

			}
		});
		try {
			removeRequest.send();
		} catch (RequestException e) {
			header.info("C'� un disturbo nella forza... " + e.getMessage());
		}
	}

	@Override
	public void onDownloadStatusUpdate(DownloadBean download) {
		SearchUI ui = Registry.get("searchui");
		ui.updateDowloadRow(download);
	}
}
