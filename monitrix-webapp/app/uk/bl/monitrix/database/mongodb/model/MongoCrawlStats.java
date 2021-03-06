package uk.bl.monitrix.database.mongodb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import uk.bl.monitrix.database.mongodb.MongoProperties;
import uk.bl.monitrix.model.CrawlStats;
import uk.bl.monitrix.model.CrawlStatsUnit;

/**
 * A MongoDB-backed implementation of {@link CrawlStats}.
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public class MongoCrawlStats implements CrawlStats {
	
	protected DBCollection collection; 
	
	// A simple in-memory buffer for quick stats lookups
	protected Map<Long, MongoCrawlStatsUnit> cache = new HashMap<Long, MongoCrawlStatsUnit>();
	
	public MongoCrawlStats(DB db) {
		this.collection = db.getCollection(MongoProperties.COLLECTION_CRAWL_STATS);
		
		// Collection is indexed by timestamp (will be skipped automatically if index exists)
		this.collection.ensureIndex(new BasicDBObject(MongoProperties.FIELD_CRAWL_STATS_TIMESTAMP, 1));
	}

	@Override
	public Iterator<CrawlStatsUnit> getCrawlStats() {
		final DBCursor cursor = collection.find().sort(new BasicDBObject(MongoProperties.FIELD_CRAWL_STATS_TIMESTAMP, 1));
		return new Iterator<CrawlStatsUnit>() {
			@Override
			public boolean hasNext() {
				return cursor.hasNext();
			}

			@Override
			public CrawlStatsUnit next() {
				return new MongoCrawlStatsUnit(cursor.next());
			}

			@Override
			public void remove() {
				cursor.remove();	
			}
		};
	}

	/**
	 * FIXME crawl_id ignored at present.
	 */
	@Override
	public Iterator<CrawlStatsUnit> getCrawlStats(String crawl_id) {
		return this.getCrawlStats();
	}

	/**
	 * FIXME crawl_id ignored at present.
	 */
	@Override
	public CrawlStatsUnit getStatsForTimestamp(long timestamp, String crawl_id) {
		if (cache.containsKey(timestamp))
			return cache.get(timestamp);
		
		DBObject query = new BasicDBObject(MongoProperties.FIELD_CRAWL_STATS_TIMESTAMP, timestamp);
		DBObject result = collection.findOne(query);
		
		if (result == null) {
			return null;
		} else {
			MongoCrawlStatsUnit stats = new MongoCrawlStatsUnit(result);
			cache.put(timestamp, stats);
			return stats;
		}
	}

	@Override
	public List<CrawlStatsUnit> getMostRecentStats(int n) {
		DBCursor cursor = collection.find().sort(new BasicDBObject(MongoProperties.FIELD_CRAWL_STATS_TIMESTAMP, -1)).limit(n);
		
		List<CrawlStatsUnit> recent = new ArrayList<CrawlStatsUnit>();
		while(cursor.hasNext())
			recent.add(new MongoCrawlStatsUnit(cursor.next()));

		return recent;
	}

}
