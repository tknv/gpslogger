package lab.tknv.gpslogger.loggers.customurl;


import android.test.suitebuilder.annotation.SmallTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class CustomUrlRequestTest {

    @Test
    public void getHttpMethod_IsAlwaysCapitalized(){
        CustomUrlRequest cur = new CustomUrlRequest("http://example.com", "get", "", "", "","");
        String expected = "GET";
        assertThat("HTTP Method is always capitalized", cur.getHttpMethod(), is(expected));

        cur = new CustomUrlRequest("http://example.com", "puT", "", "", "","");
        expected = "PUT";
        assertThat("HTTP Method is always capitalized", cur.getHttpMethod(), is(expected));
    }

    @Test
    public void getHttpMethod_CustomMethodNames_AlwaysCapitalized(){
        CustomUrlRequest cur = new CustomUrlRequest("http://example.com", "blaH", "", "", "","");
        String expected = "BLAH";
        assertThat("Custom HTTP Method should also be capitalized", cur.getHttpMethod(), is(expected));
    }

    @Test
    public void removeCredentialsFromUrl_CredentialsPresent_RemovedFromUrl(){
        CustomUrlRequest cur = new CustomUrlRequest("http://bob:hunter2@example.com/%LOG");
        String expected = "http://example.com/%LOG";
        assertThat("Credentials are removed from URL", cur.getLogURL(), is(expected));
    }

    @Test
    public void addBasicAuthorizationHeader_CredentialsPresent(){
        CustomUrlRequest cur = new CustomUrlRequest("http://bob:hunter2@example.com/%LOG");
        String expected = "Basic Ym9iOmh1bnRlcjI=";
        assertThat("Authorization header is present", cur.getHttpHeaders().get("Authorization"), is(expected));
    }

    @Test
    public void addBasicAuthorizationHeader_EmailUsername(){
        CustomUrlRequest cur = new CustomUrlRequest("http://bob@example.com:hunter2@example.com/%LOG");
        String expected = "Basic Ym9iQGV4YW1wbGUuY29tOmh1bnRlcjI=";

        assertThat("Authorization header uses email as username", cur.getHttpHeaders().get("Authorization"), is(expected));
    }

    @Test
    public void removeCredentialsFromUrl_EmailUsername_RemovedFromUrl(){
        CustomUrlRequest cur = new CustomUrlRequest("http://bob@example.com:hunter2@example.com/%LOG");
        String expected = "http://example.com/%LOG";
        assertThat("Credentials are removed from URL", cur.getLogURL(), is(expected));
    }

    @Test
    public void addBasicAuthorization_CredentialsNotPresent(){
        CustomUrlRequest cur = new CustomUrlRequest("http://example.com/%SER");
        assertThat("No authorization header is present", cur.getHttpHeaders().get("Authorization"), is(nullValue()));
    }


    @Test
    public void getHeadersFromTextBlock_SingleHeader(){
        String headers = "X-Custom: 17";
        HashMap<String, String> expected = new HashMap<String, String>();
        expected.put("X-Custom", "17");

        CustomUrlRequest cur = new CustomUrlRequest("http://example.com/", "GET", "", headers, "","");
        assertThat("Single line header is parsed", cur.getHttpHeaders(), is(expected));
    }

    @Test
    public void getHeadersFromTextBlock_MultipleHeaders(){
        String headers = "Content-Type: application/json\nAuthorization: Basic 123984234=\nApiToken: 12346";
        HashMap<String,String> expectedMap = new HashMap<>();
        expectedMap.put("Content-Type", "application/json");
        expectedMap.put("Authorization", "Basic 123984234=");
        expectedMap.put("ApiToken", "12346");

        CustomUrlRequest cur = new CustomUrlRequest("http://example.com", "GET", "", headers, "","");

        assertThat("Headers map created from text block", cur.getHttpHeaders(), is(expectedMap));
    }

    @Test
    public void getHeadersFromTextBlock_NoHeaders(){
        String headers = "";
        HashMap<String,String> expectedMap = new HashMap<String,String>();

        CustomUrlRequest cur = new CustomUrlRequest("http://example.com/", "GET", "", headers, "","");
        assertThat("Deal with empty headers line", cur.getHttpHeaders(), is(expectedMap));

        headers = "\n    \n :";
        cur = new CustomUrlRequest("http://example.com/", "GET", "", headers, "","");
        assertThat("Whitespace results in blank headers", cur.getHttpHeaders(), is(expectedMap));
    }


    @Test
    public void getHeadersFromTextBlock_SpuriousInput(){
        String headers = "blah blah \n ploopity ploopity";
        HashMap<String,String> expectedMap = new HashMap<String,String>();

        CustomUrlRequest cur = new CustomUrlRequest("http://example.com", "GET", "", headers, "","");

        assertThat("Headers block should be properly formatted with newlines and colons", cur.getHttpHeaders(), is(expectedMap));
    }


    @Test
    public void getHeadersFromTextBlock_AuthorizationPresent(){
        String headers = "Authorization: Basic aaaaaaa=";
        HashMap<String,String> expectedMap = new HashMap<String,String>();
        expectedMap.put("Authorization", "Basic aaaaaaa=");

        CustomUrlRequest cur = new CustomUrlRequest("http://bob:hunter2@example.com", "GET", "", headers, "","");

        assertThat("User defined headers should override URL authorization", cur.getHttpHeaders(), is(expectedMap));

    }

    @Test
    public void getCredentialsFromConstructor_WhenPresent_AddBasicAuthorization(){

        CustomUrlRequest cur = new CustomUrlRequest("http://example.com", "GET", "", "", "alice", "hunter1");

        HashMap<String,String> expectedMap = new HashMap<String,String>();
        expectedMap.put("Authorization", "Basic YWxpY2U6aHVudGVyMQ==");

        assertThat("Custom URL basic auth dialog should produce Basic Auth header", cur.getHttpHeaders(), is(expectedMap));
    }

    @Test
    public void getCredentialsFromConstructor_WhenEmpty_NothingAdded(){
        CustomUrlRequest cur = new CustomUrlRequest("http://example.com", "GET", "", "", "","");
        HashMap<String,String> expectedMap = new HashMap<String,String>();
        assertThat("When no credentials passed in constructor, no auth header is added", cur.getHttpHeaders(), is(expectedMap));
    }



    @Test
    public void getCredentialsFromConstructor_WhenCredentialsInUrl_ShouldOverrideUrlCredentials(){
        CustomUrlRequest cur = new CustomUrlRequest("http://bob:hunter2@example.com", "GET", "", "", "alice","hunter1");
        HashMap<String,String> expectedMap = new HashMap<String,String>();
        expectedMap.put("Authorization", "Basic YWxpY2U6aHVudGVyMQ==");

        assertThat("When credentials passed in constructor, auth header should override credentials from URL", cur.getHttpHeaders(), is(expectedMap));
    }


    @Test
    public void getCredentialsFromConstructor_WhenOneCredentialPresent_ShouldNotAddAuthHeader(){
        CustomUrlRequest cur = new CustomUrlRequest("http://example.com", "GET", "", "", "alice","");
        HashMap<String,String> expectedMap = new HashMap<String,String>();

        assertThat("When only username is passed in, no auth header is added", cur.getHttpHeaders(), is(expectedMap));
    }

    @Test
    public void getCredentialsFromConstructor_WhenAuthHeaderAlreadyPresent_ShouldNotOverrideUserDefinedHeader(){
        String headers = "Authorization: Basic aaaaaaa=";
        CustomUrlRequest cur = new CustomUrlRequest("http://example.com", "GET", "", headers, "alice","");

        HashMap<String,String> expectedMap = new HashMap<String,String>();
        expectedMap.put("Authorization", "Basic aaaaaaa=");

        assertThat("When user has already defined Basic Auth header, it should not get overridden", cur.getHttpHeaders(), is(expectedMap));
    }

}
