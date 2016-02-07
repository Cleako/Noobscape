package org.rscdaemon.server.util;

import java.util.HashSet;

public class Flags {

	public static HashSet<String> flags;

	static {
		String[] flagStrings = { "AD", "AE", "AF", "AG", "AI", "AL", "AM",
				"AN", "AO", "AR", "AS", "AT", "AU", "AW", "AX", "AZ", "BA",
				"BB", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BM", "BN",
				"BO", "BR", "BS", "BT", "BV", "BW", "BY", "BZ", "CA", "CC",
				"CD", "CF", "CG", "CH", "CI", "CK", "CL", "CM", "CN", "CO",
				"CR", "CS", "CU", "CV", "CX", "CY", "CZ", "DE", "DJ", "DK",
				"DM", "DO", "DZ", "EC", "EE", "EG", "EH", "EN", "ER", "ES",
				"ET", "EU", "FA", "FI", "FJ", "FK", "FM", "FO", "FR", "GA",
				"GB", "GD", "GE", "GF", "GH", "GI", "GL", "GM", "GN", "GP",
				"GQ", "GR", "GS", "GT", "GU", "GW", "GY", "HK", "HM", "HN",
				"HR", "HT", "HU", "ID", "IE", "IL", "IN", "IO", "IQ", "IR",
				"IS", "IT", "JM", "JO", "JP", "KE", "KG", "KH", "KI", "KM",
				"KN", "KP", "KR", "KW", "KY", "KZ", "LA", "LB", "LC", "LI",
				"LK", "LR", "LS", "LT", "LU", "LV", "LY", "MA", "MC", "MD",
				"ME", "MG", "MH", "MK", "ML", "MM", "MN", "MO", "MP", "MQ",
				"MR", "MS", "MT", "MU", "MV", "MW", "MX", "MY", "MZ", "NA",
				"NC", "NE", "NF", "NG", "NI", "NL", "NO", "NP", "NR", "NU",
				"NZ", "OM", "PA", "PE", "PF", "PG", "PH", "PK", "PL", "PM",
				"PN", "PR", "PS", "PT", "PW", "PY", "QA", "RE", "RO", "RS",
				"RU", "RW", "SA", "SB", "SC", "SD", "SE", "SG", "SH", "SI",
				"SJ", "SK", "SL", "SM", "SN", "SO", "SR", "ST", "SV", "SY",
				"SZ", "TC", "TD", "TF", "TG", "TH", "TJ", "TK", "TL", "TM",
				"TN", "TO", "TR", "TT", "TV", "TW", "TZ", "UA", "UG", "UM",
				"US", "UY", "UZ", "VA", "VC", "VE", "VG", "VI", "VN", "VU",
				"WA", "WF", "WS", "YE", "YT", "ZA", "ZM", "ZW" };
		flags = new HashSet<String>();
		for (String s : flagStrings) {
			flags.add(s);
		}
	}
}
