package it.luigibifulco.xdcc4j.ui.client.search.view;

import it.luigibifulco.xdcc4j.common.model.DownloadBean;
import it.luigibifulco.xdcc4j.ui.client.Registry;
import it.luigibifulco.xdcc4j.ui.client.search.event.DownloadRequestHandler;
import it.luigibifulco.xdcc4j.ui.client.search.view.HeaderUi.ShowDownloadsEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.github.gwtbootstrap.client.ui.AccordionGroup;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.ProgressBar;
import com.github.gwtbootstrap.client.ui.Row;
import com.github.gwtbootstrap.client.ui.base.ProgressBarBase.Color;
import com.github.gwtbootstrap.client.ui.base.ProgressBarBase.Style;
import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.github.gwtbootstrap.client.ui.event.HiddenEvent;
import com.github.gwtbootstrap.client.ui.event.ShownEvent;
import com.github.gwtbootstrap.client.ui.incubator.Table;
import com.github.gwtbootstrap.client.ui.incubator.TableHeader;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class SearchUI extends Composite {

	public final static Type<DownloadRequestHandler> DOWNLOAD_REQUEST_START = new Type<DownloadRequestHandler>();

	public final static Type<DownloadRequestHandler> DOWNLOAD_UPDATE = new Type<DownloadRequestHandler>();

	public static class DownloadRequestEvent extends
			GwtEvent<DownloadRequestHandler> {

		private DownloadBean download;

		private String type;

		public DownloadRequestEvent(DownloadBean d) {
			this.download = d;
		}

		public DownloadRequestEvent(DownloadBean d, String type) {
			this.download = d;
			this.type = type;
		}

		@Override
		public com.google.gwt.event.shared.GwtEvent.Type<DownloadRequestHandler> getAssociatedType() {
			return DOWNLOAD_REQUEST_START;
		}

		@Override
		protected void dispatch(DownloadRequestHandler handler) {
			if (type.equals("cancel")) {
				handler.onDownloadCancel(download);
				return;
			} else if (type.equals("remove")) {
				handler.onDownloadRemove(download);
				return;
			} else if (type.equals("update")) {
				handler.onDownloadStatusUpdate(download);
				return;
			}
			handler.onDownloadRequest(download);

		}
	}

	private static SearchUIUiBinder uiBinder = GWT
			.create(SearchUIUiBinder.class);

	interface SearchUIUiBinder extends UiBinder<Widget, SearchUI> {
	}

	public SearchUI() {
		currentResult = new HashMap<Integer, DownloadBean>();
		progressBars = new HashMap<String, ProgressBar>();
		labels = new HashMap<String, Label>();
		initWidget(uiBinder.createAndBindUi(this));
		listHeader
				.setText("Inserisci una o pi� parole chiave per avviare la ricerca..");
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {

			@Override
			public void execute() {
				fireEvent(new ShowDownloadsEvent());

			}
		});

	}

	@UiField
	AccordionGroup accordionDownloads;

	@UiField
	AccordionGroup accordionSearch;

	@UiField
	Table list;

	@UiField
	Table listDownloads;

	private Map<String, ProgressBar> progressBars;

	private Map<String, Label> labels;

	@UiField
	TableHeader listHeader;

	@UiField
	TableHeader listDownloadsHeader;

	private Map<Integer, DownloadBean> currentResult;

	public void clearSearchResult() {
		listHeader
				.setText("Inserisci una parola chiave per avviare la ricerca...");
		int count = list.getWidgetCount();

		for (int i = 0; i < count; i++) {
			try {
				list.remove(i);
			} catch (Exception e) {
				continue;
			}

		}
		list.clear();
	}

	public void clearDownloadsResult() {
		listDownloadsHeader.setText("");
		int count = list.getWidgetCount();

		for (int i = 0; i < count; i++) {
			try {
				list.remove(i);
			} catch (Exception e) {
				continue;
			}

		}
		listDownloads.clear();
	}

	public void showSearchResult() {
		accordionSearch.show();
	}

	public void setSearchResult() {
		accordionDownloads.hide();
		listHeader.setText("Risultato della ricerca:");
		currentResult.clear();
		Map<String, DownloadBean> map = Registry.get("searchresult");
		Collection<DownloadBean> beans = map.values();
		int cnt = 0;
		for (DownloadBean db : beans) {
			if (db == null)
				continue;
			currentResult.put(cnt, db);
			cnt++;
		}
		Set<Integer> idxs = currentResult.keySet();
		for (Integer idx : idxs) {
			Row r = new Row();
			// r.setHeight("20px");
			DownloadBean b = currentResult.get(idx);
			com.github.gwtbootstrap.client.ui.Label lID = new com.github.gwtbootstrap.client.ui.Label(
					"" + idx);
			lID.setVisible(false);
			com.github.gwtbootstrap.client.ui.Label l = new com.github.gwtbootstrap.client.ui.Label(
					b.getDesc() + " - " + b.getServer() + " - "
							+ b.getChannel() + " - " + b.getResource());
			l.addStyleName("text-center");
			l.setHeight("20px");
			l.addStyleName("rowResult");
			r.addStyleName("text-left");
			r.setPullRight(false);
			r.setMarginTop(3);
			l.setType(LabelType.INFO);
			r.add(l);
			l.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					com.github.gwtbootstrap.client.ui.Label l = (Label) event
							.getSource();
					Row r = (Row) l.getParent();
					com.github.gwtbootstrap.client.ui.Label lid = (Label) r
							.getWidget(1);
					String id = lid.getText();
					DownloadBean d = currentResult.get(Integer.valueOf(id));
					fireEvent(new DownloadRequestEvent(d));
				}
			});
			r.add(lID);
			list.add(r);
		}

	}

	public void removeDownloadRow() {

	}

	public void addDownloadRow() {

	}

	public void updateDowloadRow(DownloadBean downloadBean) {
		ProgressBar pb = progressBars.get(downloadBean.getId());
		Label l = labels.get(downloadBean.getId());
		if (pb == null) {
			return;
		}
		pb.setPercent((int) downloadBean.getPerc());
		pb.setText(downloadBean.getPerc() + "%");
		String msg = downloadBean.getStatusMessage();
		if (msg == null) {
			msg = "";
		}
		String slabel = downloadBean.getDesc() + " - " + downloadBean.getPerc()
				+ "%" + " - " + (downloadBean.getRate() / 1000) + "KB/s";
		if (!msg.isEmpty()) {
			slabel += " --> " + msg;
		}
		l.setText(slabel);
	}

	public void showDownloads() {
		accordionSearch.hide();
		progressBars.clear();
		Map<String, DownloadBean> downloads = Registry.get("downloads");
		listDownloadsHeader.setText("I tuoi downloads: ");
		Collection<DownloadBean> beans = downloads.values();
		for (final DownloadBean downloadBean : beans) {
			String msg = downloadBean.getStatusMessage() == null ? ""
					: downloadBean.getStatusMessage();
			String slabel = downloadBean.getDesc() + " - "
					+ downloadBean.getPerc() + "%" + " - "
					+ (downloadBean.getRate() / 1000) + "KB/s ";
			String append = "--> " + downloadBean.getStatusMessage();
			if (!msg.isEmpty()) {
				slabel += append;
			}
			Row r = new Row();
			com.github.gwtbootstrap.client.ui.Label info = new com.github.gwtbootstrap.client.ui.Label(
					slabel);
			labels.put(downloadBean.getId(), info);
			com.github.gwtbootstrap.client.ui.Label lID = new com.github.gwtbootstrap.client.ui.Label(
					"" + downloadBean.getId());
			lID.setVisible(false);
			// TODO: showinfo btn
			Button restart = new Button();
			restart.setText("Restart download");
			restart.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					fireEvent(new DownloadRequestEvent(downloadBean));

				}
			});
			Button cancelDownload = new Button();
			cancelDownload.setText("Cancel download");
			cancelDownload.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					fireEvent(new DownloadRequestEvent(downloadBean, "cancel"));

				}
			});

			Button remDownload = new Button();
			remDownload.setText("Remove download");
			remDownload.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					fireEvent(new DownloadRequestEvent(downloadBean, "remove"));

				}
			});
			ProgressBar pb = new ProgressBar();
			pb.setPercent((int) downloadBean.getPerc());
			pb.setColor(Color.INFO);
			pb.setType(Style.ANIMATED);
			pb.addStyleName("custom-progress");
			pb.addDomHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					Window.alert(event.getSource().toString());

				}
			}, ClickEvent.getType());
			pb.setText("" + downloadBean.getPerc());
			if (downloadBean.getState().equals("WORKING")) {
				pb.setColor(Color.SUCCESS);
				pb.setType(Style.ANIMATED);
				// pb.setStyle(Style.ANIMATED);

			} else if (downloadBean.getState().equals("ABORTED")) {
				pb.setColor(Color.DANGER);
				pb.setType(Style.STRIPED);

				// pb.setStyle(Style.ANIMATED);

			} else if (downloadBean.getState().equals("RUNNABLE")) {
				pb.setColor(Color.DEFAULT);
				pb.setType(Style.ANIMATED);
				// pb.setPercent(99);
			} else if (downloadBean.getState().equals("IDLE")) {
				pb.setColor(Color.WARNING);
				pb.setType(Style.STRIPED);
				// pb.setPercent(99);
			} else {
				pb.setColor(Color.INFO);
				pb.setType(Style.ANIMATED);
				// pb.setPercent(99);
			}
			r.add(restart);
			r.add(cancelDownload);
			r.add(remDownload);
			r.add(info);
			r.add(pb);

			progressBars.put(downloadBean.getId(), pb);

			listDownloads.add(r);
			accordionDownloads.show();
		}

	}

	@UiHandler("accordionSearch")
	public void onShownSearch(ShownEvent event) {
		try {
			clearDownloadsResult();
		} catch (Exception e) {

		}
		setSearchResult();
	}

	@UiHandler("accordionSearch")
	public void onHideSearch(HiddenEvent event) {
		// clearSearchResult();
	}

	@UiHandler("accordionDownloads")
	public void onShownDownloads(ShownEvent event) {
		try {
			clearDownloadsResult();
		} catch (Exception e) {

		}
		fireEvent(new ShowDownloadsEvent());
	}

}
