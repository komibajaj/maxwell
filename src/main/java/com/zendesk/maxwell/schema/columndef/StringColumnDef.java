package com.zendesk.maxwell.schema.columndef;

import java.nio.charset.Charset;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringEscapeUtils;

import com.google.code.or.common.util.MySQLConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringColumnDef extends ColumnDef {
	public String encoding;

	static final Logger LOGGER = LoggerFactory.getLogger(StringColumnDef.class);
	public StringColumnDef () { }
	public StringColumnDef(String name, String type, int pos, String encoding) {
		super(name, type, pos);
		this.encoding = encoding;
	}

	public void setDefaultEncoding(String e) {
		if ( this.encoding == null )
		  this.encoding = e;
	}

	@Override
	public boolean matchesMysqlType(int type) {
		return type == MySQLConstants.TYPE_BLOB ||
			   type == MySQLConstants.TYPE_VARCHAR ||
			   type == MySQLConstants.TYPE_STRING;
	}

	@Override
	public String toSQL(Object value) {
		byte[] b = (byte[]) value;

		if ( encoding.equals("utf8") || encoding.equals("utf8mb4")) {
			return quoteString(new String(b));
		} else {
			return "x'" +  Hex.encodeHexString( b ) + "'";
		}
	}

	// this could obviously be more complete.
	private Charset charsetForEncoding() {
		switch(encoding.toLowerCase()) {
		case "utf8": case "utf8mb4":
			return Charset.forName("UTF-8");
		case "latin1": case "ascii":
			return Charset.forName("ISO-8859-1");
		case "ucs2":
			return Charset.forName("UTF-16");
		default:
			LOGGER.warn("warning: unhandled character set '" + encoding + "'");
			return null;
		}
	}
	@Override
	public Object asJSON(Object value) {

		if ( value instanceof String ) {
			return value;
		}

		byte[] b = (byte[])value;
		if ( encoding.equals("binary") ) {
			return Base64.encodeBase64String(b);
		} else {
			return new String(b, charsetForEncoding());
		}
	}

	private String quoteString(String s) {
		String escaped = StringEscapeUtils.escapeSql(s);
		escaped = escaped.replaceAll("\n", "\\\\n");
		escaped = escaped.replaceAll("\r", "\\\\r");
		return "'" + escaped + "'";
	}
}
