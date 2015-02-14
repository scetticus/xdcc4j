package it.luigibifulco.xdcc4j.ft.impl;

import it.biffi.jirc.bot.BotException;
import it.biffi.jirc.bot.FileTransferBot;
import it.biffi.jirc.bot.FileTransferConfig;
import it.biffi.jirc.bot.listener.FileTransferListener;
import it.luigibifulco.xdcc4j.common.model.XdccRequest;
import it.luigibifulco.xdcc4j.ft.XdccFileTransfer;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.jibble.pircbot.DccFileTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XdccFileTransferImpl implements XdccFileTransfer {
	private final static Logger LOGGER = LoggerFactory
			.getLogger(XdccFileTransferImpl.class);

	private TransferState state;

	protected FileTransferBot bot;

	private final XdccRequest request;

	private Timer t;

	private long connectTimeout;

	public XdccFileTransferImpl(XdccRequest request, long connectTimeout,
			long requestTTL) throws BotException {
		state = TransferState.IDLE;
		this.request = request;
		this.request.setTtl(requestTTL);
		this.connectTimeout = connectTimeout;

	}

	protected void init() throws BotException {
		LOGGER.info("Request: " + request);
		bot = new FileTransferBot(false);

		String name = UUID.randomUUID().toString();

		LOGGER.info("connecting to host: " + request.getHost());
		boolean connected = bot.connect("[Bot" + name.substring(0, 5) + "]",
				request.getHost(), connectTimeout);
		if (connected) {
			state = TransferState.RUNNABLE;
			LOGGER.info("connected");
			FileTransferConfig config = new FileTransferConfig();

			String chan = request.getChannel().replace("#", "");
			config.setSourceChannel(chan);
			config.setSourcePeer(request.getPeer());

			config.setInputCommand("xdcc send #" + request.getResource());
			// TODO pass timeout to start method
			bot.start(config);
		}
	}

	@Override
	public boolean start(final FileTransferStatusListener l) {
		if (state == TransferState.IDLE) {
			try {
				init();
			} catch (BotException e) {
				return false;
			}
		}
		if (state == TransferState.RUNNABLE) {
			final DccFileTransfer transfer = bot.requestPacket(
					request.getTtl(), new FileTransferListener() {

						@Override
						public void onFinish(Exception e) {
							state = TransferState.FINISHED;
							if (e == null) {
								System.out.println("FINISH TRANSFER SUCCESS");
								l.onFinish();
							} else {
								System.out.println("FINISH TRANSFER ERROR: "
										+ e.getMessage());
								l.onError(e);
								// e.printStackTrace();
							}

						}

						@Override
						public void onPreStartUpdate(String data) {
							l.onStatusUpdate(data);
							System.out.println("onPreStartUpdate:" + data);

						}
					});
			if (transfer != null) {
				state = TransferState.WORKING;

				File file = new File(request.getDestination() + "/"
						+ transfer.getFile().getName());
				LOGGER.info("Receiveing file: " + file.getAbsolutePath());

				transfer.receive(file, true);
				l.onStart();
				t = new Timer("PollTrasnferState", true);
				t.scheduleAtFixedRate(new TimerTask() {

					@Override
					public void run() {
						int perc = (int) transfer.getProgressPercentage();
						int rate = (int) transfer.getTransferRate();
						System.out.println("Trasnfer state: " + perc + "%"
								+ "rate: " + rate);
						l.onProgress(perc, rate);
					}
				}, 0, 5000);
				return true;
			} else {
				LOGGER.info("packet requested but transfer not started");
				state = TransferState.FINISHED;
				l.onFinish();
				return false;
			}
		} else {
			LOGGER.info("state of transfer is not in runnable");
			state = TransferState.FINISHED;
			l.onFinish();
			return false;
		}

	}

	@Override
	public boolean cancel() {
		state = TransferState.ABORTED;
		if (t != null) {
			t.cancel();
			t.purge();
		}
		t = null;
		if (bot != null) {
			bot.stop();
		}
		// request = null;
		return true;
	}

	@Override
	public boolean pause() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean resume() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TransferState getState() {
		return state;
	}

	@Override
	public boolean restart() {
		this.cancel();
		try {
			this.init();
		} catch (BotException e) {
			return false;
		}
		return true;
	}

}
