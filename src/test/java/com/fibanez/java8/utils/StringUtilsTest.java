package com.fibanez.java8.utils;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author fibanez
 */
public class StringUtilsTest {

    @Test
    public void when_null_expeted_emptyString() throws Exception {
        String result = StringUtils.getLastPathPart(null);
        assertThat(result, is(StringUtils.EMPTY));
    }

    @Test
    public void when_empty_expeted_emptyString() throws Exception {
        String result = StringUtils.getLastPathPart(StringUtils.EMPTY);
        assertThat(result, is(StringUtils.EMPTY));
    }

    @Test
    public void when_baseDomain_expeted_emptyString() throws Exception {
        String result = StringUtils.getLastPathPart("http://www.baseurl.com");
        assertThat(result, is(StringUtils.EMPTY));
    }

    @Test
    public void when_noUrl_expeted_emptyString() throws Exception {
        String result = StringUtils.getLastPathPart("baseurl.com/id/");
        assertThat(result, is(StringUtils.EMPTY));
    }

    @Test
    public void when_badlyFormedUrl_expeted_emptyString() throws Exception {
        String result = StringUtils.getLastPathPart("www.baseurl.com/id/");
        assertThat(result, is(StringUtils.EMPTY));
    }

    @Test
    public void when_url_expeted_lastPathPart() throws Exception {
        String result = StringUtils.getLastPathPart("http://www.baseurl.com/id/");
        assertThat(result, is("id"));
    }

    @Test
    public void when_withNameAndExtension_expeted_nameWithExtension() throws Exception {
        String result = StringUtils.getLastPathPart("http://www.baseurl.com/name.jpg");
        assertThat(result, is("name.jpg"));
    }

    @Test
    public void when_longUrl_expeted_lastPathPart() throws Exception {
        String result = StringUtils.getLastPathPart("http://www.baseurl.com/part1/part2/part3/name.jpg");
        assertThat(result, is("name.jpg"));
    }

    @Test
    public void when_wIthParameters_expeted_lastPathPart() throws Exception {
        String result = StringUtils.getLastPathPart("http://www.baseurl.com/part1/name.jpg?param=value1");
        assertThat(result, is("name.jpg"));
    }

    @Test
    public void when_endswithSlash_expeted_lastPathPart() throws Exception {
        String result = StringUtils.getLastPathPart("http://www.baseurl.com/part1/name.jpg/?param=value1");
        assertThat(result, is("name.jpg"));
    }

    @Test
    public void when_withDoubleSlash_expeted_lastPathPart() throws Exception {
        String result = StringUtils.getLastPathPart("http://www.baseurl.com//part1//name.jpg/?param=value1");
        assertThat(result, is("name.jpg"));
    }

}
