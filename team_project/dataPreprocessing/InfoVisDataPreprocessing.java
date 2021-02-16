import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class InfoVisDataPreprocessing {

	static Map<String, String> mArtistGenreMap = new HashMap<String, String>();
	static List<Record> mRecordList = new ArrayList<Record>();
	static String[] mCurFields = null;
	static String mCurId = null;
	static int mCurIdIndex = -1;
	static int mCurLineNumber = 1;

	static Map<String, Integer> mGenreCountMap = new HashMap<String, Integer>(); // for test

	public static void main(String[] args) {

		try {
			BufferedReader br = new BufferedReader(new FileReader("data_w_genres.csv"));
			String curLine = br.readLine(); // ignore first label line
			while((curLine = br.readLine()) != null) {
				mCurFields = curLine.split(",");
				mCurLineNumber++;	// for debugging
				String artist = parseArtistForGenreMap();
				if (artist == null) {
					continue;
				}
				String genre = parseGenre();
				// countGenre(genre); // 1768
				if (genre == null) {
					continue;
				}
				mArtistGenreMap.put(artist, genre);
			}
			System.out.println("* Total artist-genre map size : " + mArtistGenreMap.size());
			// System.out.println("is BTS exist? : " + mArtistGenreMap.containsKey("BTS"));
			// System.out.println("BTS genre : " + mArtistGenreMap.get("BTS"));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader("data.csv"));

			String curLine = br.readLine(); // ignore first label line
			while(curLine.endsWith(",") || curLine.endsWith(" ")) {
				curLine = curLine.substring(0, curLine.length() - 1);
			}

			// System.out.println(curLine);
			while((curLine = br.readLine()) != null) {
				mCurFields = curLine.split(",");
				updateCurIdAndIndex();
				if (mCurId == null || mCurIdIndex < 6) {
					continue;
				}

				Record record = new Record();

				record.artist = parseArtistForRecord();
				if (record.artist == null) {
					continue;
				}

				record.title = parseTitle();
				if (record.title == null) {
					continue;
				}

				record.id = mCurId;

				record.date = parseDate();
				if (record.date == null) {
					continue;
				}

				record.genre = getGenreByArtist(record.artist);
				if (record.genre == null) {
					continue;
				}

				// 7 scalable values
				record.duration_ms = parseDuration();
				if (record.duration_ms == Double.MIN_VALUE) {
					continue;
				}

				record.key = parseKey();
				if (record.key == Double.MIN_VALUE) {
					continue;
				}

				record.mode = parseMode();
				if (record.mode == Double.MIN_VALUE) {
					continue;
				}

				record.popularity = parsePopularity();
				if (record.popularity == Double.MIN_VALUE) {
					continue;
				}

				record.tempo = parseTempo();
				if (record.tempo == Double.MIN_VALUE) {
					continue;
				}

				record.loudness = parseLoudness();
				if (record.loudness == Double.MIN_VALUE) {
					continue;
				}

				record.explicit = parseExplicit();
				if (record.explicit == Double.MIN_VALUE) {
					continue;
				}

				// 7 emotional values
				record.acousticness = parseAcousticness();
				if (record.acousticness == Double.MIN_VALUE) {
					continue;
				}

				record.danceability = parseDanceability();
				if (record.danceability == Double.MIN_VALUE) {
					continue;
				}

				record.energy = parseEnergy();
				if (record.energy == Double.MIN_VALUE) {
					continue;
				}

				record.instrumentalness = parseInstrumentalness();
				if (record.energy == Double.MIN_VALUE) {
					continue;
				}

				record.liveness = parseLiveness();
				if (record.liveness == Double.MIN_VALUE) {
					continue;
				}

				record.speechiness = parseSpeechiness();
				if (record.speechiness == Double.MIN_VALUE) {
					continue;
				}

				record.valence = parseValence();
				if (record.valence == Double.MIN_VALUE) {
					continue;
				}

				// System.out.println(record.toString());
				mRecordList.add(record);
				countGenre(record.genre);
			}
			System.out.println("* Total records : " + mRecordList.size());
			System.out.println("* Genre counts : " + mGenreCountMap.size() + " genre(s)");
			for (String s : mGenreCountMap.keySet()) {
				System.out.println(s + " : " + mGenreCountMap.get(s));
			}

			writeRecordsAsJson();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void countGenre(String genre) {
		if (mGenreCountMap.containsKey(genre)) {
			mGenreCountMap.put(genre, mGenreCountMap.get(genre) + 1);
		} else {
			mGenreCountMap.put(genre, 1);
		}
	}

	private static void writeRecordsAsJson() {
		System.out.println("* Generating json obj started.");
		JSONArray jsonArr = new JSONArray();
		for (Record r : mRecordList) {
			JSONObject cur = new JSONObject();
			cur.put("title", r.title);
			cur.put("artist", r.artist);
			cur.put("genre", r.genre);
			cur.put("date", r.date);
			cur.put("id", r.id);
			cur.put("key", String.valueOf(r.key));
			cur.put("duration_ms", String.valueOf(r.duration_ms));
			cur.put("mode", String.valueOf(r.mode));
			cur.put("popularity", String.valueOf(r.popularity));
			cur.put("tempo", String.valueOf(r.tempo));
			cur.put("loudness", String.valueOf(r.loudness));
			cur.put("explicit", String.valueOf(r.explicit));
			cur.put("acousticness", String.valueOf(r.acousticness));
			cur.put("danceability", String.valueOf(r.danceability));
			cur.put("energy", String.valueOf(r.energy));
			cur.put("instrumentalness", String.valueOf(r.instrumentalness));
			cur.put("liveness", String.valueOf(r.liveness));
			cur.put("speechiness", String.valueOf(r.speechiness));
			cur.put("valence", String.valueOf(r.valence));

			jsonArr.add(cur);
		}

		//System.out.println(jsonArr.toJSONString());
		try {
			FileWriter fileWriter = new FileWriter("data.json");
			fileWriter.write(jsonArr.toJSONString());
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("* Writing json done.");
	}

	private static String parseGenre() {
		String genreStr = null;
		for (int i = 5/*min idx*/ ; i < mCurFields.length ; i++) {
			if (mCurFields[i].startsWith("[\'") || mCurFields[i].startsWith("\"[\'")) {
				for (int j = i ; j < mCurFields.length ;j ++) {
					genreStr += mCurFields[j];
				}
				break;
			}
		}

		if (genreStr == null) {
			System.out.println("Couldn't parse genre for " + mCurFields[0] + " at line "
					+ mCurLineNumber + " then ignored.");
		} else {
			System.out.println(mCurLineNumber + "] cur genre str :" + genreStr);
			if (genreStr.contains("\'k-pop\'")){
				return "k-pop";
			}
			if (genreStr.contains("\'hip hop\'")) {
				return "hiphop";
			}
			if (genreStr.contains("\'classical\'")) {
				return "classic";
			}
			if (genreStr.contains("\'rock\'")) {
				return "rock";
			}
			if (genreStr.contains("\'r&b\'")) {
				return "r&b";
			}
			if (genreStr.contains("\'jazz\'")) {
				return "jazz";
			}
			if (genreStr.contains("\'country\'")) {
				return "country";
			}
			if (genreStr.contains("\'pop\'")) {
				return "pop";
			}
		}

		return null;
	}

	private static String parseArtistForGenreMap() {
		String ret = mCurFields[0];
		ret = ret.replaceAll("[^a-zA-Z0-9 .-]", "");
		ret = ret.trim();

		if (ret.isBlank() || ret.isEmpty()) {
			// System.out.println("Couldn't parse Artist " + mCurFields[0] + " in genre data then ignored.");
			return null;
		}
		return ret;

	}

	private static void updateCurIdAndIndex() {
		String id = null;
		int index = 6;

		for (int i = 6 ; i < mCurFields.length ; i++) {
			if (!Character.isDigit(mCurFields[i].charAt(0))) {
				continue;
			}
			if (mCurFields[i].length() != 22) {
				continue;
			}
			id = mCurFields[i];
			index = i;
			break;
		}

		if (id == null) {
			//System.out.println("Couldn't parse ID then ignored.");
			mCurId = null;
			mCurIdIndex = -1;
			return;
		}

		mCurId = id;
		mCurIdIndex = index;
	}

	private static String getGenreByArtist(String artist) {
		String ret = mArtistGenreMap.get(artist);
		if (ret == null) {
			//System.out.println("Counldn't match artist of ID " + mCurId + " to genre then ignored.");
		}
		return ret;
	}

	private static double parseValence() {
		double ret = Double.parseDouble(mCurFields[ mCurFields.length - 2 ]);
		if (ret > 1 || ret < 0) {
			//System.out.println("Invalid Valence of ID " + mCurId + " then ignored.");
			return Double.MIN_VALUE;
		}
		// System.out.println(String.format("%.7f", ret));
		return ret;
	}

	private static double parseSpeechiness() {
		double ret = Double.parseDouble(mCurFields[ mCurFields.length - 4 ]);
		if (ret > 1 || ret < 0) {
			//System.out.println("Invalid Speechiness of ID " + mCurId + " then ignored.");
			return Double.MIN_VALUE;
		}
		// System.out.println(String.format("%.7f", ret));
		return ret;
	}

	private static double parseLiveness() {
		double ret = Double.parseDouble(mCurFields[ mCurIdIndex + 3]);
		if (ret > 1 || ret < 0) {
			//System.out.println("Invalid Liveness of ID " + mCurId + " then ignored.");
			return Double.MIN_VALUE;
		}
		//System.out.println(String.format("%.7f", ret));
		return ret;
	}

	private static double parseInstrumentalness() {
		double ret = Double.parseDouble(mCurFields[ mCurIdIndex + 1]);
		if (ret > 1 || ret < 0) {
			//System.out.println("Invalid Instrumentalness of ID " + mCurId + " then ignored.");
			return Double.MIN_VALUE;
		}
		//System.out.println(String.format("%.7f", ret));
		return ret;
	}

	private static double parseEnergy() {
		double ret = Double.parseDouble(mCurFields[ mCurIdIndex - 2]);
		if (ret > 1 || ret < 0) {
			//System.out.println("Invalid Energy of ID " + mCurId + " then ignored.");
			return Double.MIN_VALUE;
		}
		//System.out.println(String.format("%.7f", ret));
		return ret;
	}

	private static double parseDanceability() {
		double ret = Double.parseDouble(mCurFields[ mCurIdIndex - 4]);
		if (ret > 1 || ret < 0) {
			//System.out.println("Invalid Danceability of ID " + mCurId + " then ignored.");
			return Double.MIN_VALUE;
		}
		// System.out.println(String.format("%.7f", ret));
		return ret;
	}

	private static double parseExplicit() {
		double ret = Double.parseDouble(mCurFields[ mCurIdIndex - 1]);
		if (!(ret == 1 || ret == 0)) {
			//System.out.println("Invalid Explicit of ID " + mCurId + " then ignored.");
			return Double.MIN_VALUE;
		}
		//System.out.println(String.format("%.7f", ret));
		return ret;

	}

	private static double parseLoudness() {
		double ret = Double.parseDouble(mCurFields[ mCurIdIndex + 4]);
		if (ret == 0) {
			//System.out.println("Invalid Loudness of ID " + mCurId + " then ignored.");
			return Double.MIN_VALUE;
		}
		//System.out.println(String.format("%.7f", ret));
		return ret;
	}

	private static double parseTempo() {
		double ret = Double.parseDouble(mCurFields[ mCurFields.length - 3 ]);
		if (ret <= 0) {
			//System.out.println("Invalid Tempo of ID " + mCurId + " then ignored.");
			return Double.MIN_VALUE;
		}
		//System.out.println(String.format("%.7f", ret));
		return ret;
	}

	private static double parsePopularity() {
		double ret = -1;
		try {
			ret = Double.parseDouble(mCurFields[ mCurFields.length - 6 ]);
		} catch (NumberFormatException e) {
			 //System.out.println("Couldn't parse Popularity of ID " + mCurId + " from "
			//		 + mCurFields[ mCurFields.length - 6 ]);
			return Double.MIN_VALUE;
		}
		if (ret < 0) {
			//System.out.println("Invalid Popularity of ID " + mCurId + " then ignored.");
			return Double.MIN_VALUE;
		}
		// System.out.println(String.format("%.7f", ret));
		return ret;
	}

	private static double parseMode() {
		double ret = Double.parseDouble(mCurFields[ mCurIdIndex + 5]);
		if (!(ret == 1 || ret == 0)) {
			//System.out.println("Invalid Mode of ID " + mCurId + " then ignored.");
			return Double.MIN_VALUE;
		}
		// System.out.println(String.format("%.7f", ret));
		return ret;
	}

	private static double parseKey() {
		double ret = Double.parseDouble(mCurFields[ mCurIdIndex + 2]);
		if (ret < -1) {
			//System.out.println("Invalid Key of ID " + mCurId + " then ignored.");
			return Double.MIN_VALUE;
		}
		// System.out.println(String.format("%.7f", ret));
		return ret;
	}

	private static double parseDuration() {
		double ret = Double.parseDouble(mCurFields[ mCurIdIndex - 3]);
		if (ret < 1000) {
			//System.out.println("Invalid Duration of ID " + mCurId + " then ignored.");
			return Double.MIN_VALUE;
		}
		// System.out.println(String.format("%.7f", ret));
		return ret;
	}

	private static String parseDate() {
		String ret = mCurFields[ mCurIdIndex + 8 ];
		if (!(ret.length() == 10 || ret.length() == 4)) {
			//System.out.println("Couldn't parse Date of ID " + mCurId + " then ignored.");
			return null;
		}

		if (ret.length() == 4) {
			ret += "-01-01";
		}

		if (!ret.matches("^(\\d{4})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])$")) {
			//System.out.println("Invalid Date of ID " + mCurId + " then ignored.");
			return null;
		}

		return ret;
	}


	private static String parseTitle() {
		int idIdx = mCurIdIndex;
		if (idIdx == -1) {
			return null;
		}
		int titleIndex = idIdx + 6;
		String ret = mCurFields[ titleIndex ];
		ret = ret.replaceAll("é", "e");
		ret = ret.replaceAll("¿", "");
		ret = ret.replaceAll("ñ", "n");
		ret = ret.replaceAll("ó", "o");
		ret = ret.replaceAll(" É", "e");
		ret = ret.replaceAll("í", "i");
		ret = ret.replaceAll("[^a-zA-Z0-9 .-] ", "");
		if (ret.indexOf("(") != -1) {
			ret = ret.substring(0, ret.indexOf("("));
		}
		ret = ret.trim();
		if (ret.startsWith("\"") ) {
			ret = ret.substring(1, ret.length());
			if (ret.endsWith("\"")) {
				ret = ret.substring(0, ret.length() - 1);
			}
		}
		ret = ret.replaceAll("\"\"", "\"");

		if (ret.isEmpty() || ret.isBlank() ||
				!Charset.forName("US-ASCII").newEncoder().canEncode(ret)) {
			//System.out.println("Couldn't parse Title of ID " + mCurId + " then ignored.");
			return null;
		}

		// System.out.println("ID : " + parseId(fields) + " title : " + ret);

		return ret;
	}

	private static String parseArtistForRecord() throws Exception {
		String ret = mCurFields[1].replaceAll("é", "e");
		ret = ret.replaceAll("[^a-zA-Z0-9 .-]", "");
		ret = ret.trim();

		if (ret.isBlank()) {
			// System.out.println("Couldn't parse Artist of ID " + mCurId + " then ignored.");
			return null;
		}
		return ret;
	}

	private static double parseAcousticness() throws Exception {
		double ret = Double.parseDouble(mCurFields[0]);
		if (ret > 1 || ret < 0) {
			//System.out.println("Invalid Acousticness of ID " + mCurId + " then ignored.");
			return Double.MIN_VALUE;
		}
		// System.out.println(String.format("%.7f", ret));
		return ret;
	}

	private static int getIdFieldIndex(String[] fields) {
		for (int i = 6 ; i < fields.length ; i++) {
			if (!Character.isDigit(fields[i].charAt(0))) {
				continue;
			}
			if (fields[i].length() != 22) {
				continue;
			}
			return i;
		}
		return -1;
	}
}

class Record {
	// Note. ignore 'year' field due to date. add genre field.
	// general values
	String title;
	String artist;	// care ONE artist only.
	String id;
	String date;
	String genre;

	// 7 scalable values
	double duration_ms;
	double key;
	double mode;
	double popularity;
	double tempo;
	double loudness;
	double explicit;

	// 7 emotional values
	double acousticness;
	double danceability;
	double energy;
	double instrumentalness;
	double liveness;
	double speechiness;
	double valence;

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("* Title : ").append(title).append("\n");
		buf.append("- Artist : ").append(artist).append("\n");
		buf.append("- Genre : ").append(genre).append("\n");
		buf.append("- Date : ").append(date).append("\n");
		buf.append("- id : ").append(id).append("\n");
		buf.append("- Scalable data").append("\n");
		buf.append("  1. key : ").append(key).append("\n");
		buf.append("  2. duration_ms : ").append(duration_ms).append("\n");
		buf.append("  3. mode : ").append(mode).append("\n");
		buf.append("  4. popularity : ").append(popularity).append("\n");
		buf.append("  5. tempo : ").append(tempo).append("\n");
		buf.append("  6. loudness : ").append(loudness).append("\n");
		buf.append("  7. explicit : ").append(explicit).append("\n");
		buf.append("- Emotional data").append("\n");
		buf.append("  1. acousticness : ").append(acousticness).append("\n");
		buf.append("  2. danceability : ").append(danceability).append("\n");
		buf.append("  3. energy : ").append(energy).append("\n");
		buf.append("  4. instrumentalness : ").append(instrumentalness).append("\n");
		buf.append("  5. liveness : ").append(liveness).append("\n");
		buf.append("  6. speechiness : ").append(speechiness).append("\n");
		buf.append("  7. valence : ").append(valence).append("\n");
		return buf.toString();
	}
}
