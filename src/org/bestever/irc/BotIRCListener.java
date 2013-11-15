package org.bestever.irc;

import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

@SuppressWarnings("rawtypes")
public class BotIRCListener extends ListenerAdapter {

	private BotIRC botirc;
	
	public BotIRCListener(BotIRC botirc) {
		this.botirc = botirc;
	}
	
	@Override
	public void onMessage(MessageEvent event) throws Exception {
		User u = event.getUser();
		botirc.onMessage(event.getChannel().toString(), u.getNick().toString(), u.getLogin(), u.getHostmask(), event.getMessage());
	}
}
