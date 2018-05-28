package com.swproject.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.swproject.domain.CrawlerNews;
import com.swproject.domain.CrawlerSNS;
import com.swproject.domain.CrawlerVO;
import com.swproject.domain.SearchCriteria;
import com.swproject.persistence.CrawlDAO;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

@Service
public class CrawlServiceImpl implements CrawlService {

	@Inject
	private CrawlDAO dao;

	@Inject
	private CrawlService service;

	public String time() {

		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // yyyy.MM.dd.
																										// HH:mm:ss
		String now = formatter.format(new Date());
		return now;

	}

	public void split(CrawlerVO sns, Status temp1) {
		String crawl = temp1.getText().toString();
		String content, addr;
		int i = 0;
		if (crawl.indexOf("http") != -1) {
			i = crawl.indexOf("http");
			content = crawl.substring(0, i);
			addr = crawl.substring(i + 1);
			if(spaceCheck(addr) == true){
				int index = addr.indexOf(' ') + 1;
				String link = addr.substring(0, index);
				sns.setS_Addr('h' + link);
			}else{
				sns.setS_Addr('h' + addr);
			}
			sns.setS_Content(content);
		}
	}
	
	public boolean spaceCheck(String spaceCheck){
		for(int i=0; i<spaceCheck.length(); i++){
			if(spaceCheck.charAt(i) == ' ')
				return true;
		}
		return false;
	}

	@Override
	public void create1(CrawlerVO crawl) throws Exception {
		dao.create1(crawl);
	}

	@Override
	public void create2(CrawlerVO crawl) throws Exception {
		dao.create2(crawl);
	}

	@Override
	public List<CrawlerVO> listCrawl1(CrawlerVO cn) throws Exception {
		CrawlerNews Craw = new CrawlerNews();
		Craw.setURL("https://news.google.com/?hl=ko&gl=KR&ceid=KR:ko");
		/* url ���� : https://news.google.co.kr/news/?ned=kr*/ // �ڿ� �ΰ����� kr�� ������ ������ ������������ ����´�.
		Craw.setDoc(Jsoup.connect(Craw.getURL()).get());
		Craw.setEl(Craw.getDoc().select("c-wiz.Erv9te"));
		Elements temp = Craw.getEl();
		
		if(!temp.equals(null)){
			Elements ctitle = temp.select("div.ZulkBc.qNiaOd span");
			Elements csource = temp.select("div.PNwZO.zhsNkd span");
			Elements tempurl = temp.select("a.ipQwMb");
			//Elements cimg = temp.select("img.tvs3Id.dIH98c");
			
			for(int i=0; i<ctitle.size(); i++){
				cn.setC_Group("News");
				cn.setC_Time(time());
				
				// �̹����� ��� �־�� �ϳ�...
				/*if(cimg.eq(i).contains("<img")){
					cn.setN_IMG(cimg.eq(i).attr("src"));
				}else{
					cn.setN_IMG("NO IMG");
				}*/
				
				cn.setN_Source(csource.eq(i).text());
				cn.setN_Title(ctitle.eq(i).text());
				
				String curl = tempurl.eq(i).select("a.ipQwMb").attr("href").replace(".", "https://news.google.com");
				cn.setURL(curl);
				service.create1(cn);
			}
		}
		
		// foreach�� ������ �ʾ� �� ���ƾ���
		/*for (Element temp : Craw.getEl()) {
			cn.setC_Group("News");
			cn.setN_IMG(temp.select("div.xrnccd img").attr("src").toString());
			
			// ���������簡 ������ �پ���°�찡 �ִ� �߰� ���������縦 �߶� set������.
			String sp = temp.select("span.KbnJ8").text();
			
			if(spaceCheck(sp) == true){
				int index = sp.indexOf(' ') + 1;
				//System.out.println("sp.length() = "+sp.length());
				//System.out.println("index = "+index);
				String Source = sp.substring(0, index);
				//System.out.println("Source = "+Source);
				cn.setN_Source(Source);
			}
			//cn.setN_Source(sp);
			
			//cn.setN_Source(temp.select("span.IH8C7b").text());
			
			//System.out.println("�̰� : "+temp.select("div.ZulkBc.qNiaOd span").eq(1).text().toString());
			
			cn.setN_Title(temp.select("div.ZulkBc.qNiaOd span").eq(numbr).text().toString());
			numbr++;
			
			String tempurl = temp.select("a.ipQwMb").attr("href").toString();
			String curl = tempurl.replace(".", "https://news.google.com");
			cn.setURL(curl);
			
			cn.setC_Time(time());
			
			service.create1(cn);
		}*/

		return dao.listCrawl1(cn);
	}

	@Override
	public List<CrawlerVO> listCrawl2(CrawlerVO sns) throws Exception {
		CrawlerSNS CS = new CrawlerSNS();
		CS.setTwitter(TwitterFactory.getSingleton());
		Twitter gt = CS.getTwitter();
		CS.setUser(gt.verifyCredentials());
		CS.setList(gt.getHomeTimeline());
		List<Status> cl = CS.getList();
		List<String> media = new ArrayList<String>();
		
		for (Status te : cl){ // img Url ��������(��ũ �̹����� �������� �Ȱ������µ�...)
			for(MediaEntity me : te.getMediaEntities()){
				media.add(me.getMediaURL());
			}
		}
		
		for (Status temp1 : cl) {
			sns.setC_Group("SNS");
			sns.setS_User(temp1.getUser().getScreenName().toString());
			/*if(!temp1.getMediaEntities().equals(null)){ // ��°������ ������ �̹����� ������ �����´�.
				for(MediaEntity me : temp1.getMediaEntities()){
					sns.setS_Addr(me.getMediaURL());
				}
			}else{
				split(sns, temp1);
			}*/
			split(sns, temp1);
			sns.setC_Time(time());
			
			service.create2(sns);
		}
		return dao.listCrawl2(sns);
	}

	@Override
	public List<CrawlerVO> listSearchCriteria(SearchCriteria cri) throws Exception {
		return dao.listSearch(cri);
	}

	@Override
	public int listSearchCount(SearchCriteria cri) throws Exception {
		return dao.listSearchCount(cri);
	}

}
