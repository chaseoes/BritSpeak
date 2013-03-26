package me.chaseoes.britspeak;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class BritSpeak extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        
        try {
            final File lib = new File(getDataFolder(), "jsoup.jar");
            if (!lib.exists()) {
                JarUtils.extractFromJar(lib.getName(), lib.getAbsolutePath());
            }

            if (!lib.exists()) {
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            addClassPath(JarUtils.getJarUrl(lib));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void addClassPath(final URL url) throws IOException {
        final URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        final Class<URLClassLoader> sysclass = URLClassLoader.class;
        try {
            final Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { url });
        } catch (final Throwable t) {
            t.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        StringBuilder sb = new StringBuilder();
        for (String s : event.getMessage().split(" ")) {
            sb.append(" " + translateWord(s));
        }
        event.setMessage(sb.toString().trim());
    }

    private String translateWord(String word) {
        if (word.equalsIgnoreCase("fire") || word.equalsIgnoreCase("in") || word.equalsIgnoreCase("me")) {
            return word;
        }
        try {
            Document doc = Jsoup.connect("http://www.translatebritish.com/search.php?st=" + word.replaceAll("\\p{Punct}", "") + "&submit=GO").get();
            Element el = doc.getElementsByTag("table").get(2).getElementsByTag("tbody").get(0).getElementsByTag("tr").get(0).getElementsByTag("td").get(0);
            List<String> superPowers = new ArrayList<String>();
            for (Element en : el.getElementsByTag("b")) {
                superPowers.add(en.text());
            }
            return superPowers.get(new Random().nextInt(superPowers.size()));
        } catch (Exception e) {
            return word;
        }
    }

}
