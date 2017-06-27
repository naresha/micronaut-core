/*
 * Copyright 2017 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.particleframework.http;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a media type. See https://www.iana.org/assignments/media-types/media-types.xhtml and https://tools.ietf.org/html/rfc2046
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public class MediaType implements CharSequence {

    /**
     * A wildcard media type representing all types
     */
    public static final MediaType ALL = new MediaType("*/*", "all");
    /**
     * Form encoded data: application/x-www-form-urlencoded
     */
    public static final MediaType FORM = new MediaType("application/x-www-form-urlencoded", "form");
    /**
     * Multi part form data: multipart/form-data
     */
    public static final MediaType MULTIPART_FORM = new MediaType("multipart/form-data", "multipartForm");
    /**
     * HTML: text/html
     */
    public static final MediaType HTML = new MediaType("text/html");
    /**
     * XHTML: application/xhtml+xml
     */
    public static final MediaType XHTML = new MediaType("application/xhtml+xml", "html");
    /**
     * XML: application/xml
     */
    public static final MediaType XML = new MediaType("application/xml");
    /**
     * JSON: application/json
     */
    public static final MediaType JSON = new MediaType("application/json");
    /**
     * XML: text/xml
     */
    public static final MediaType TEXT_XML = new MediaType("text/xml");
    /**
     * JSON: text/json
     */
    public static final MediaType TEXT_JSON = new MediaType("text/json");
    /**
     * HAL JSON: application/hal+json
     */
    public static final MediaType HAL_JSON = new MediaType("application/hal+json");
    /**
     * HAL XML: application/hal+xml
     */
    public static final MediaType HAL_XML = new MediaType("application/hal+xml");
    /**
     * Atom: application/atom+xml
     */
    public static final MediaType ATOM_XML = new MediaType("application/atom+xml");
    /**
     * VND Error: application/vnd.error+json
     */
    public static final MediaType VND_ERROR = new MediaType("application/vnd.error+json");

    private static final String QUALITY_RATING = "1.0";
    private static final String CHARSET_PARAMETER = "charset";
    private static final BigDecimal QUALITY_RATING_NUMBER = new BigDecimal("1.0");
    private static final String Q_PARAMETER = "q";
    private static final String V_PARAMETER = "v";

    protected final String name;
    protected final String subtype;
    protected final String type;
    protected final String fullName;
    protected final String extension;
    protected final Map<String, String> parameters;

    private BigDecimal qualityNumberField;

    /**
     * Constructs a new media type for the given string
     *
     * @param name The name of the media type. For example application/json
     */
    public MediaType(String name) {
        this(name, null, Collections.emptyMap());
    }

    /**
     * Constructs a new media type for the given string and parameters
     *
     * @param name The name of the media type. For example application/json
     * @param params The parameters
     */
    public MediaType(String name, Map<String, String> params) {
        this(name, null, params);
    }

    /**
     * Constructs a new media type for the given string and extension
     *
     * @param name The name of the media type. For example application/json
     * @param extension The extension of the file using this media type if it differs from the subtype
     */
    public MediaType(String name, String extension) {
        this(name, extension, Collections.emptyMap());
    }

    /**
     * Constructs a new media type for the given string and extension
     *
     * @param name The name of the media type. For example application/json
     * @param extension The extension of the file using this media type if it differs from the subtype
     */
    public MediaType(String name, String extension, Map<String, String> params) {
        this.fullName = name;
        this.parameters = new LinkedHashMap<>();
        this.parameters.put(Q_PARAMETER, QUALITY_RATING);
        if(name == null) {
            throw new IllegalArgumentException("Argument [name] cannot be null");
        }
        if(name.indexOf(';') > -1) {
            String[] tokenWithArgs = name.split(";");
            name = tokenWithArgs[0];
            String[] paramsList = Arrays.copyOfRange(tokenWithArgs, 1, tokenWithArgs.length);
            for(String param : paramsList) {
                int i = param.indexOf('=');
                if (i > -1) {
                    parameters.put(param.substring(0, i).trim(), param.substring(i+1).trim() );
                }
            }
        }
        this.name = name;
        int i = name.indexOf('/');
        if(i > -1) {
            this.type = name.substring(0, i);
            this.subtype = name.substring(i + 1, name.length());
        }
        else {
            throw new IllegalArgumentException("Invalid mime type: " + name);
        }

        if(extension != null) {
            this.extension = extension;
        }
        else {
            int j = subtype.indexOf('+');
            if(j > -1) {
                this.extension = subtype.substring(j + 1);
            }
            else {
                this.extension = subtype;
            }
        }
        if(params != null) {
            parameters.putAll(params);
        }
    }

    /**
     * @return Full name with parameters
     */
    public String getFullName() {
        return fullName;
    }
    /**
     * @return The name of the mime type without any parameters
     */
    public String getName() {
        return name;
    }

    /**
     * @return The type of the media type. For example for application/hal+json this would return "application"
     */
    public String getType() {
        return this.type;
    }

    /**
     * @return The subtype. For example for application/hal+json this would return "hal+json"
     */
    public String getSubtype() {
        return this.subtype;
    }

    /**
     * @return The extension. For example for application/hal+json this would return "json"
     */
    public String getExtension() {
        return extension;
    }

    /**
     * @return The parameters to the media type
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * @return The quality of the Mime type
     */
    public String getQuality() {
        return parameters.getOrDefault("q", QUALITY_RATING);
    }

    /**
     * @return The quality in BigDecimal form
     */
    public BigDecimal getQualityAsNumber() {
        if(this.qualityNumberField == null) {
            this.qualityNumberField = getOrConvertQualityParameterToBigDecimal(this);
        }
        return this.qualityNumberField;
    }

    /**
     * @return The version of the Mime type
     */
    String getVersion() {
        return parameters.getOrDefault(V_PARAMETER, null);
    }


    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaType mediaType = (MediaType) o;

        return name.equals(mediaType.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    private BigDecimal getOrConvertQualityParameterToBigDecimal(MediaType mt) {
        BigDecimal bd;
        try {
            String q = mt.parameters.getOrDefault(Q_PARAMETER, null);
            if(q == null) return QUALITY_RATING_NUMBER;
            else {
                bd = new BigDecimal(q);
            }
            return bd;
        } catch (NumberFormatException e) {
            bd = QUALITY_RATING_NUMBER;
            return bd;
        }
    }
}
