package com.yash.ammatv;

import java.util.ArrayList;
import java.util.List;

public class ChannelItem {
    public final String title;
    public final String teluguTitle;
    public final String category;
    public final String searchQuery;
    public final String[] mustContainAny;
    public final String[] mustNotContain;
    public final int colorHex;

    public ChannelItem(String title, String teluguTitle, String category, String searchQuery,
                       String[] mustContainAny, String[] mustNotContain, int colorHex) {
        this.title = title;
        this.teluguTitle = teluguTitle;
        this.category = category;
        this.searchQuery = searchQuery;
        this.mustContainAny = mustContainAny;
        this.mustNotContain = mustNotContain;
        this.colorHex = colorHex;
    }

    public static List<ChannelItem> getAllTeluguChannels() {
        List<ChannelItem> list = new ArrayList<>();

        // 1. Entertainment (General)
        list.add(new ChannelItem("Zee Telugu", "జీ తెలుగు", "Entertainment", "Zee Telugu",
                new String[]{"ZEE TELUGU"}, new String[]{"CINEMALU"}, 0xFFE65100));
        list.add(new ChannelItem("Star Maa", "స్టార్ మా", "Entertainment", "Star Maa",
                new String[]{"STAR MAA HD", "STAR MAA"}, new String[]{"MOVIES", "GOLD", "MUSIC"}, 0xFFC62828));
        list.add(new ChannelItem("ETV Telugu", "ఈటీవీ తెలుగు", "Entertainment", "ETV",
                new String[]{"ETV TELUGU", "ETV HD", "ETV"}, new String[]{"CINEMA", "PLUS", "LIFE", "ABHIRUCHI", "BALA", "ANDHRA", "TELANGANA", "CPETV"}, 0xFF1565C0));
        list.add(new ChannelItem("Gemini TV", "జెమినీ టీవీ", "Entertainment", "Gemini",
                new String[]{"GEMINI TV", "GEMINI HD", "GEMINI"}, new String[]{"MOVIES", "COMEDY", "MUSIC", "LIFE", "BASKET", "MESTRE"}, 0xFF2E7D32));
        list.add(new ChannelItem("ETV Plus", "ఈటీవీ ప్లస్", "Entertainment", "ETV Plus",
                new String[]{"ETV PLUS"}, new String[]{}, 0xFF0277BD));
        list.add(new ChannelItem("Gemini Life", "జెమినీ లైఫ్", "Entertainment", "Gemini Life",
                new String[]{"GEMINI LIFE"}, new String[]{}, 0xFF388E3C));
        list.add(new ChannelItem("ETV Abhiruchi", "ఈటీవీ అభిరుచి", "Entertainment", "Abhiruchi",
                new String[]{"ABHIRUCHI"}, new String[]{}, 0xFFAD1457));
        list.add(new ChannelItem("ETV Life", "ఈటీవీ లైఫ్", "Entertainment", "ETV Life",
                new String[]{"ETV LIFE"}, new String[]{}, 0xFF00838F));
        list.add(new ChannelItem("Vanitha TV", "వనిత టీవీ", "Entertainment", "Vanitha",
                new String[]{"VANITHA"}, new String[]{}, 0xFF8E24AA));

        // 2. Movies & Comedy
        list.add(new ChannelItem("Zee Cinemalu", "జీ సినిమalu", "Movies", "Zee Cinemalu",
                new String[]{"ZEE CINEMALU"}, new String[]{}, 0xFFD84315));
        list.add(new ChannelItem("Star Maa Movies", "స్టార్ మా మూవీస్", "Movies", "Star Maa Movies",
                new String[]{"STAR MAA MOVIES"}, new String[]{}, 0xFFB71C1C));
        list.add(new ChannelItem("Star Maa Gold", "స్టార్ మా గోల్డ్", "Movies", "Star Maa Gold",
                new String[]{"STAR MAA GOLD"}, new String[]{}, 0xFFF57F17));
        list.add(new ChannelItem("Gemini Movies", "జెమినీ మూవీస్", "Movies", "Gemini Movies",
                new String[]{"GEMINI MOVIES"}, new String[]{}, 0xFF1B5E20));
        list.add(new ChannelItem("Gemini Comedy", "జెమినీ కామెడీ", "Movies", "Gemini Comedy",
                new String[]{"GEMINI COMEDY"}, new String[]{}, 0xFF6A1B9A));
        list.add(new ChannelItem("ETV Cinema", "ఈటీవీ సినిమా", "Movies", "ETV Cinema",
                new String[]{"ETV CINEMA"}, new String[]{}, 0xFF283593));
        list.add(new ChannelItem("Telugu Cinema", "తెలుగు సినిమా", "Movies", "Telugu Cinema",
                new String[]{"TELUGU CINEMA"}, new String[]{}, 0xFF4527A0));

        // 3. News
        list.add(new ChannelItem("TV9 Telugu", "టీవీ9 తెలుగు", "News", "TV9",
                new String[]{"TV9 TELUGU", "TELEGU| TV9", "TELUGU| TV9", "TV9 NEWS"}, new String[]{"KANNADA", "MARATHI", "GUJARATI", "BHARATVARSH", "BANGLA"}, 0xFF1A237E));
        list.add(new ChannelItem("NTV Telugu", "ఎన్టీవీ తెలుగు", "News", "NTV",
                new String[]{"NTV TELUGU", "TELEGU| NTV", "TELUGU| NTV", "NTV NEWS", "NTV"}, new String[]{"AF|", "CI|", "KE|", "NG|", "TG|", "LNTV", "BANGLA", "UGANDA"}, 0xFF0D47A1));
        list.add(new ChannelItem("V6 News", "వీ6 న్యూస్", "News", "V6",
                new String[]{"V6 NEWS", "V6"}, new String[]{"TV6"}, 0xFFC62828));
        list.add(new ChannelItem("Sakshi TV", "సాక్షి టీవీ", "News", "Sakshi",
                new String[]{"SAKSHI"}, new String[]{}, 0xFF01579B));
        list.add(new ChannelItem("ABN Andhra Jyothi", "ఏబీఎన్ ఆంధ్రజ్యోతి", "News", "ABN",
                new String[]{"ABN ANDHRA", "ABN NEWS", "TELEGU| ABN", "TELUGU| ABN"}, new String[]{"3ABN", "NG|", "AR|"}, 0xFFB71C1C));
        list.add(new ChannelItem("TV5 News", "టీవీ5 న్యూస్", "News", "TV5",
                new String[]{"TV5 NEWS", "TV5 TELUGU", "TELEGU| TV5", "TELUGU| TV5"}, new String[]{"MONDE", "KANNADA", "PHILIPPINES"}, 0xFF311B92));
        list.add(new ChannelItem("ETV Andhra Pradesh", "ఈటీవీ ఆంధ్రప్రదేశ్", "News", "ETV Andhra",
                new String[]{"ETV ANDHRA"}, new String[]{}, 0xFF1976D2));
        list.add(new ChannelItem("ETV Telangana", "ఈటీవీ తెలంగాణ", "News", "ETV Telangana",
                new String[]{"ETV TELANGANA"}, new String[]{}, 0xFF0288D1));
        list.add(new ChannelItem("10TV News", "10టీవీ న్యూస్", "News", "10TV",
                new String[]{"10TV", "10 TV"}, new String[]{}, 0xFFD32F2F));
        list.add(new ChannelItem("T News", "టీ న్యూస్", "News", "T News",
                new String[]{"T NEWS", "TNEWS"}, new String[]{}, 0xFFAD1457));
        list.add(new ChannelItem("HM TV", "హెచ్ఎం టీవీ", "News", "HMTV",
                new String[]{"HMTV", "HM TV"}, new String[]{}, 0xFF00695C));
        list.add(new ChannelItem("Mahaa News", "మహా న్యూస్", "News", "Mahaa",
                new String[]{"MAHAA NEWS", "MAHA NEWS"}, new String[]{"MAHA MOVIES"}, 0xFFBF360C));
        list.add(new ChannelItem("CVR News", "సీవీఆర్ న్యూస్", "News", "CVR News",
                new String[]{"CVR NEWS"}, new String[]{}, 0xFF455A64));
        list.add(new ChannelItem("Raj News Telugu", "రాజ్ న్యూస్", "News", "Raj News Telugu",
                new String[]{"RAJ NEWS TELUGU", "RAJ NEWS"}, new String[]{"TAMIL", "KANNADA", "MALAYALAM"}, 0xFF5D4037));
        list.add(new ChannelItem("BRK News", "బీఆర్కే న్యూస్", "News", "BRK",
                new String[]{"BRK"}, new String[]{}, 0xFF37474F));
        list.add(new ChannelItem("Prime9 News", "ప్రైమ్9 న్యూస్", "News", "Prime9",
                new String[]{"PRIME9", "PRIME 9"}, new String[]{}, 0xFF880E4F));

        // 4. Devotional & Music & Kids
        list.add(new ChannelItem("Bhakti TV", "భక్తి టీవీ", "Devotional & Music", "Bhakti TV",
                new String[]{"BHAKTI TV", "BHAKTHI TV"}, new String[]{}, 0xFFE65100));
        list.add(new ChannelItem("SVBC TTD", "ఎస్వీబీసీ తిరుమల", "Devotional & Music", "SVBC",
                new String[]{"SVBC"}, new String[]{}, 0xFFF57C00));
        list.add(new ChannelItem("Subhavarta TV", "శుభవార్త టీవీ", "Devotional & Music", "Subhavarta",
                new String[]{"SUBHAVARTA", "SUBHAVAARTHA"}, new String[]{}, 0xFF6D4C41));
        list.add(new ChannelItem("Hindu Dharmam", "హిందూ ధర్మం", "Devotional & Music", "Hindu Dharmam",
                new String[]{"HINDU DHARMAM"}, new String[]{}, 0xFFEF6C00));
        list.add(new ChannelItem("Gemini Music", "జెమినీ మ్యూజిక్", "Devotional & Music", "Gemini Music",
                new String[]{"GEMINI MUSIC"}, new String[]{}, 0xFF2E7D32));
        list.add(new ChannelItem("Star Maa Music", "స్టార్ మా మ్యూజిక్", "Devotional & Music", "Maa Music",
                new String[]{"MAA MUSIC"}, new String[]{}, 0xFFC62828));
        list.add(new ChannelItem("Raj Music Telugu", "రాజ్ మ్యూజిక్ తెలుగు", "Devotional & Music", "Raj Music Telugu",
                new String[]{"RAJ MUSIC TELUGU", "RAJ MUSIC"}, new String[]{"TAMIL", "KANNADA"}, 0xFF7B1FA2));
        list.add(new ChannelItem("Telugu Songs HD", "తెలుగు పాటలు", "Devotional & Music", "Telugu Songs",
                new String[]{"TELUGU SONGS"}, new String[]{}, 0xFF512DA8));
        list.add(new ChannelItem("Kushi TV", "ఖుషి టీవీ", "Devotional & Music", "Kushi",
                new String[]{"KUSHI"}, new String[]{}, 0xFF00897B));
        list.add(new ChannelItem("ETV Balabharat", "ఈటీవీ బాలభారత్", "Devotional & Music", "Balabharat",
                new String[]{"BALABHARAT", "BALA BHARAT"}, new String[]{}, 0xFF039BE5));
        list.add(new ChannelItem("History 18 Telugu", "హిస్టరీ 18 తెలుగు", "Devotional & Music", "History 18 Telugu",
                new String[]{"HISTORY 18 TELUGU", "HISTORY TV18"}, new String[]{}, 0xFF546E7A));
        list.add(new ChannelItem("Discovery Telugu", "డిస్కవరీ తెలుగు", "Devotional & Music", "Discovery World",
                new String[]{"DISCOVERY"}, new String[]{}, 0xFF006064));

        return list;
    }
}
