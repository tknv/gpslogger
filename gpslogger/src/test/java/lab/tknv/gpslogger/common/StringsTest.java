package lab.tknv.gpslogger.common;

import android.content.Context;
import android.os.Build;
import android.test.suitebuilder.annotation.SmallTest;

import lab.tknv.gpslogger.BuildConfig;
import com.mendhak.gpslogger.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class StringsTest {



    @Test
    public void HtmlDecode_WhenEntitiesPresent_EntitiesAreDecoded(){


        String actual = Strings.htmlDecode("Bert &amp; Ernie are here. They wish to &quot;talk&quot; to you.");
        String expected = "Bert & Ernie are here. They wish to \"talk\" to you.";
        assertThat("HTML Decode did not decode everything", actual, is(expected));

        actual = Strings.htmlDecode(null);
        expected = null;

        assertThat("HTML Decode should handle null input", actual, is(expected));

    }



    @Test
    public void GetIsoDateTime_DateObject_ConvertedToIso() {

        String actual = Strings.getIsoDateTime(new Date(1417726140000l));
        String expected = "2014-12-04T20:49:00.000Z";
        assertThat("Conversion of date to ISO string", actual, is(expected));
    }

    @Test
    public void GetIsoDateTime_HigherResolutionMilliseconds_ConvertedToIso() {

        String actual = Strings.getIsoDateTime(new Date(1417726140001l));
        String expected = "2014-12-04T20:49:00.001Z";
        assertThat("Conversion of date to ISO string", actual, is(expected));
    }



    @Test
    public void getIsoCalendarDate_DateObject_ConvertedToIso() {

        String actual = Strings.getIsoCalendarDate(new Date(1417726140000l));
        String expected = "2014-12-04";
        assertThat("Conversion of date to ISO string", actual, is(expected));
    }

    @Test
    public void getIsoCalendarDate_HigherResolutionMilliseconds_ConvertedToIso() {

        String actual = Strings.getIsoCalendarDate(new Date(1417726140001l));
        String expected = "2014-12-04";
        assertThat("Conversion of date to ISO string", actual, is(expected));
    }



    @Test
    public void CleanDescription_WhenAnnotationHasHtml_HtmlIsRemoved() {
        String content = "This is some annotation that will end up in an " +
                "XML file.  It will either <b>break</b> or Bert & Ernie will alert up" +
                "and cause all sorts of mayhem. Either way, it won't \"work\"";

        String expected = "This is some annotation that will end up in an " +
                "XML file.  It will either bbreak/b or Bert &amp; Ernie will alert up" +
                "and cause all sorts of mayhem. Either way, it won't &quot;work&quot;";

        String actual = Strings.cleanDescriptionForXml(content);

        assertThat("Clean Description should remove characters", actual, is(expected));
    }


    @Test
    public void GetFormattedCustomFileName_Serial_ReplacedWithBuildSerial() {
        PreferenceHelper ph = mock(PreferenceHelper.class);
        Calendar gc = mock(Calendar.class);

        String expected = "basename_" + Build.SERIAL;
        String actual = Strings.getFormattedCustomFileName("basename_%ser", gc, ph);
        assertThat("Static file name %SER should be replaced with Build Serial", actual, is(expected));

    }

    @Test
    public void GetFormattedCustomFileName_Version_ReplacedWithBuildVersion() {

        PreferenceHelper ph = mock(PreferenceHelper.class);
        Calendar gc = mock(Calendar.class);

        String expected = "basename_" + BuildConfig.VERSION_NAME;
        String actual = Strings.getFormattedCustomFileName("basename_%ver", gc,ph);
        assertThat("Static file name %VER should be replaced with Build Version", actual, is(expected));

    }

    @Test
    public void GetFormattedCustomFileName_HOUR_ReplaceWithPaddedHour(){

        PreferenceHelper ph = mock(PreferenceHelper.class);
        Calendar gc = mock(Calendar.class);
        when(gc.get(Calendar.HOUR_OF_DAY)).thenReturn(4);

        String expected = "basename_04";
        String actual = Strings.getFormattedCustomFileName("basename_%HOUR", gc, ph);
        assertThat("%HOUR 4 AM should be replaced with 04", actual, is(expected));

        when(gc.get(Calendar.HOUR_OF_DAY)).thenReturn(23);
        expected = "basename_23";
        actual = Strings.getFormattedCustomFileName("basename_%HOUR", gc, ph);
        assertThat("%HOUR 11PM should be relpaced with 23", actual, is(expected));

        when(gc.get(Calendar.HOUR_OF_DAY)).thenReturn(0);
        expected = "basename_00";
        actual = Strings.getFormattedCustomFileName("basename_%HOUR", gc, ph);
        assertThat("%HOUR 0 should be relpaced with 00", actual, is(expected));

    }

    @Test
    public void GetFormattedCustomFileName_MIN_ReplaceWithPaddedMinute(){

        PreferenceHelper ph = mock(PreferenceHelper.class);
        Calendar gc = mock(Calendar.class);
        when(gc.get(Calendar.HOUR_OF_DAY)).thenReturn(4);
        when(gc.get(Calendar.MINUTE)).thenReturn(7);

        String actual = Strings.getFormattedCustomFileName("basename_%HOUR%MIN", gc, ph);
        String expected = "basename_0407";
        assertThat(" %MIN 7 should be replaced with 07", actual, is(expected));

        when(gc.get(Calendar.HOUR_OF_DAY)).thenReturn(0);
        when(gc.get(Calendar.MINUTE)).thenReturn(0);

        actual = Strings.getFormattedCustomFileName("basename_%HOUR%MIN", gc, ph);
        expected = "basename_0000";
        assertThat(" %MIN 0 should be replaced with 00", actual, is(expected));

    }

    @Test
    public void GetFormattedCustomFileName_YEARMONDAY_ReplaceWithYearMonthDay(){

        PreferenceHelper ph = mock(PreferenceHelper.class);
        Calendar gc = mock(Calendar.class);
        when(gc.get(Calendar.YEAR)).thenReturn(2016);
        when(gc.get(Calendar.MONTH)).thenReturn(Calendar.FEBRUARY);
        when(gc.get(Calendar.DAY_OF_MONTH)).thenReturn(1);

        String actual = Strings.getFormattedCustomFileName("basename_%YEAR%MONTH%DAY", gc, ph);
        String expected = "basename_20160201";
        assertThat("Year 2016 Month February Day 1 should be replaced with 20160301", actual, is(expected));


        when(gc.get(Calendar.MONTH)).thenReturn(Calendar.DECEMBER);
        actual = Strings.getFormattedCustomFileName("basename_%YEAR%MONTH%DAY", gc, ph);
        expected = "basename_20161201";
        assertThat("December month should be replaced with 12", actual, is(expected));

        when(gc.get(Calendar.MONTH)).thenReturn(0);
        actual = Strings.getFormattedCustomFileName("basename_%YEAR%MONTH%DAY", gc, ph);
        expected = "basename_20160101";
        assertThat("Zero month should be replaced with 1", actual, is(expected));

    }

    @Test
    public void GetFormattedCustomFileName_PROFILE_ReplaceWithProfileName(){

        PreferenceHelper ph = mock(PreferenceHelper.class);
        Calendar gc = mock(Calendar.class);
        when(ph.getCurrentProfileName()).thenReturn("OOMPALOOMPA");

        String actual = Strings.getFormattedCustomFileName("basename_%PROFILE",gc, ph);
        String expected = "basename_OOMPALOOMPA";

        assertThat("Profile replaced with profile name", actual, is(expected));

    }


    public void getFormattedCustomFileName_DAYNAME_ReplaceWithThreeLetterDayName(){
        PreferenceHelper ph = mock(PreferenceHelper.class);
        GregorianCalendar greg = new GregorianCalendar();
        greg.setTimeInMillis(1495663380828l); //24 May 2017

        String actual = Strings.getFormattedCustomFileName("basename_%DAYNAME", greg, ph);
        String expected = "basename_wed";

        assertThat("Day name substituted in file name", actual, is(expected));
    }

    @Test
    public void getFormattedCustomFileName_MONTHNAME_ReplaceWithThreeLetterDayName(){
        PreferenceHelper ph = mock(PreferenceHelper.class);
        GregorianCalendar greg = new GregorianCalendar();
        greg.setTimeInMillis(1495663380828l); //Wed, 24 May 2017

        String actual = Strings.getFormattedCustomFileName("basename_%MONTHNAME", greg, ph);
        String expected = "basename_may";

        assertThat("Month name substituted in file name", actual, is(expected));
    }




    private Context GetDescriptiveTimeString_Context(){
        Context ctx = mock(Context.class);
        when(ctx.getString(R.string.time_onesecond)).thenReturn("1 second");
        when(ctx.getString(R.string.time_halfminute)).thenReturn("&#189; minute");
        when(ctx.getString(R.string.time_oneminute)).thenReturn("1 minute");
        when(ctx.getString(R.string.time_onehour)).thenReturn("1 hour");
        when(ctx.getString(R.string.time_quarterhour)).thenReturn("15 minutes");
        when(ctx.getString(R.string.time_halfhour)).thenReturn("½ hour");
        when(ctx.getString(R.string.time_oneandhalfhours)).thenReturn("1½ hours");
        when(ctx.getString(R.string.time_twoandhalfhours)).thenReturn("2½ hours");
        when(ctx.getString(R.string.time_hms_format)).thenReturn("%sh %sm %ss");

        return ctx;
    }

    @Test
    public void GetDescriptiveTimeString_ZeroSeconds_ReturnsEmptyString(){

        String actual = Strings.getDescriptiveDurationString(0, GetDescriptiveTimeString_Context());
        String expected = "";
        assertThat("0 seconds is empty string", actual, is(expected));
    }

    @Test
    public void GetDescriptiveTimeString_1800Seconds_ReturnsHalfHourString(){
        String actual = Strings.getDescriptiveDurationString(1800, GetDescriptiveTimeString_Context());
        String expected = "½ hour";
        assertThat("1800 seconds returns half hour", actual, is(expected));
    }

    @Test
    public void GetDescriptiveTimeString_2700Seconds_Returns45minutesString(){
        String actual = Strings.getDescriptiveDurationString(2700, GetDescriptiveTimeString_Context());
        String expected = "0h 45m 0s";
        assertThat("2700 seconds returns 45 minutes", actual, is(expected));
    }

    @Test
    public void GetDescriptiveTimeString_9001Seconds_ReturnsCorrespondingHours(){
        String actual = Strings.getDescriptiveDurationString(9001, GetDescriptiveTimeString_Context());
        String expected = "2h 30m 1s";
        assertThat("9001 seconds returns correspnding time", actual, is(expected));
    }

    @Test
    public void SanitizeMarkdownForHelpView_WhenLocalLinkPresent_ReturnsCodeWebsite() {
        String md = "### [Open source libraries used](opensourcelibraries.html)\n" +
                "\n" +
                "### [Donate Paypal](https://paypal.me/mendhak/)\r\n" +
                "This is [my screenshot](ss01.png)";
        String expected = "### [Open source libraries used](https://gpslogger.app/opensourcelibraries.html)\n" +
                "\n" +
                "### [Donate Paypal](https://paypal.me/mendhak/)\r\n" +
                "This is [my screenshot](https://gpslogger.app/ss01.png)";
        assertThat("html link replaced with full URL", Strings.getSanitizedMarkdownForFaqView(md), is(expected));
    }

    @Test
    public void SanitizeMarkdownForHelpView_WhenAbsoluteUrlLinkPresent_DoesNotReplace() {
        String md = "First line \r\n " +
                "### [Donate Paypal](https://paypal.me/mendhak/)\r\n " +
                "[Donate coffee grounds](https://example.com/) ";

        String expected = "First line \r\n " +
                "### [Donate Paypal](https://paypal.me/mendhak/)\r\n " +
                "[Donate coffee grounds](https://example.com/) ";
        assertThat("Full URL not replaced", Strings.getSanitizedMarkdownForFaqView(md), is(expected));
    }

    @Test
    public void SanitizeMarkdownForHelpView_WhenNull_ReturnsEmpty() {
        String md = null;

        String expected = "";

        assertThat("markdown returned is empty", Strings.getSanitizedMarkdownForFaqView(md), is(expected));
    }

    @Test
    public void SanitizeMarkdownForHelpView_WhenImagePresent_ImageRemoved(){
        String md = "First line \r\n " +
                "![badge](This is my badge)\r\n " +
                "[Donate coffee grounds](https://example.com/) ";

        String expected = "First line \r\n " +
                "\r\n " +
                "[Donate coffee grounds](https://example.com/) ";

        assertThat("Image in MD is removed", Strings.getSanitizedMarkdownForFaqView(md), is(expected));

    }


    @Test
    public void getDegreesMinutesSeconds_BasicConversion(){
        double lat = 11.812244d;
        String expected = "11° 48' 44.0784\" N";
        String actual = Strings.getDegreesMinutesSeconds(lat, true);

        assertThat("Degree Decimals converted to Degree Minute Seconds", actual, is(expected));
    }

    @Test
    public void getDegreesMinutesSeconds_NegativeIsSouth(){
        double lat = -16.44299d;
        String expected = "16° 26' 34.764\" S";
        String actual = Strings.getDegreesMinutesSeconds(lat, true);

        assertThat("Negative degree decimals converted to southerly degree minute second", actual, is(expected));
    }

    @Test
    public void getDegreesMinutesSeconds_Longitude_ReturnsEastWest(){
        double lon = 17.072754d;
        String expected = "17° 4' 21.9144\" E";
        String actual = Strings.getDegreesMinutesSeconds(lon, false);

        assertThat("Longitude values have east west cardinality", actual, is(expected));

        lon = -137.072754;
        expected = "137° 4' 21.9144\" W";
        actual = Strings.getDegreesMinutesSeconds(lon, false);

        assertThat("Longitude values have east west cardinality", actual, is(expected));
    }


    @Test
    public void getDegreesDecimalMinutes_BasicConversion(){
        double lat = 14.24231d;
        String expected = "14° 14.5386' N";
        String actual = Strings.getDegreesDecimalMinutes(lat, true);

        assertThat("Degree Decimals converted to Degree Decimal Minutes", actual, is(expected));
    }

    @Test
    public void getDegreesDecimalMinutes_NegativeIsSouth(){
        double lat = -54.81774d;
        String expected = "54° 49.0644' S";
        String actual = Strings.getDegreesDecimalMinutes(lat, true);

        assertThat("Negative Degree Decimals converted to southerly Degree Decimal Minutes", actual, is(expected));
    }

    @Test
    public void getDegreesDecimalMinutes_Longitude_ReturnsEastWest(){
        double lat = 101.898d;
        String expected = "101° 53.88' E";
        String actual = Strings.getDegreesDecimalMinutes(lat, false);

        assertThat("Longitude values have east west cardinality", actual, is(expected));

        lat = -111.111d;
        expected = "111° 6.66' W";
        actual = Strings.getDegreesDecimalMinutes(lat, false);

        assertThat("Longitude values have east west cardinality", actual, is(expected));
    }

    @Test
    public void getDecimalDegrees_BasicLatitude(){
        double lat = 59.28392417439d;
        String expected = "59" + getDecimalSeparator() + "283924";
        String actual = Strings.getDecimalDegrees(lat);

        assertThat("Decimal degrees formatted to 6 places", actual, is(expected));
    }


    @Test
    public void getDecimalDegrees_ShortLocation_NoPaddedZeros(){
        double lat = 59.28d;
        String expected = "59" + getDecimalSeparator() + "28";
        String actual = Strings.getDecimalDegrees(lat);

        assertThat("Decimal degrees no padded zero", actual, is(expected));
    }



    @Test
    public void getFormattedDegrees_DecimalDegrees_ReturnsDD(){
        double lat = 51.2828223838d;
        String expected = "51" + getDecimalSeparator() + "282822";

        PreferenceHelper ph = mock(PreferenceHelper.class);
        when(ph.getDisplayLatLongFormat()).thenReturn(PreferenceNames.DegreesDisplayFormat.DECIMAL_DEGREES);
        String actual = Strings.getFormattedDegrees(lat, true, ph);
        assertThat("Preference DD returns DD", actual, is(expected));
    }

    private char getDecimalSeparator() {
        DecimalFormat format= (DecimalFormat) DecimalFormat.getInstance();
        DecimalFormatSymbols symbols=format.getDecimalFormatSymbols();
        return symbols.getDecimalSeparator();
    }

    @Test
    public void getFormattedDegrees_DegreeMinuteSeconds_ReturnsDMS(){
        double lat = 51.2828223838d;
        String expected = "51° 16' 58.1606\" N";

        PreferenceHelper ph = mock(PreferenceHelper.class);
        when(ph.getDisplayLatLongFormat()).thenReturn(PreferenceNames.DegreesDisplayFormat.DEGREES_MINUTES_SECONDS);
        String actual = Strings.getFormattedDegrees(lat, true, ph);
        assertThat("Preference DMS returns DMS", actual, is(expected));

        double lon = -151.2828223838d;
        expected = "151° 16' 58.1606\" W";

        actual = Strings.getFormattedDegrees(lon, false, ph);
        assertThat("Preference DMS returns DMS", actual, is(expected));
    }

    @Test
    public void getFormattedDegrees_DegreesDecimalMinutes_ReturnsDDM(){
        double lat = 44.18923372d;
        String expected = "44° 11.354' N";

        PreferenceHelper ph = mock(PreferenceHelper.class);
        when(ph.getDisplayLatLongFormat()).thenReturn(PreferenceNames.DegreesDisplayFormat.DEGREES_DECIMAL_MINUTES);
        String actual = Strings.getFormattedDegrees(lat, true, ph);
        assertThat("Preference DDM returns DDM", actual, is(expected));

        double lon = -151.2828223838d;
        expected = "151° 16.9693' W";

        actual = Strings.getFormattedDegrees(lon, false, ph);
        assertThat("Preference DDM returns DDM", actual, is(expected));
    }

    @Test
    public void getFormattedFileName_BasicFileName_ReturnsAsIs(){
        Session sess = mock(Session.class);
        when(sess.getCurrentFileName()).thenReturn("hello");
        PreferenceHelper ph = mock(PreferenceHelper.class);

        String expected = "hello";
        String actual = Strings.getFormattedFileName(sess, ph);

        assertThat("Basic file name unchanged", actual, is(expected));

        when(sess.getCurrentFileName()).thenReturn("");
        expected = "";
        actual = Strings.getFormattedFileName(sess, ph);
        assertThat("Empty file name returns empty string", actual, is(expected));
    }

    @Test
    public void getFormattedFileName_CreateCustomFileName_ReturnsFormatted(){
        Session sess = mock(Session.class);
        when(sess.getCurrentFileName()).thenReturn("basename_%ver");
        PreferenceHelper ph = mock(PreferenceHelper.class);
        when(ph.shouldCreateCustomFile()).thenReturn(true);

        String expected = "basename_" + BuildConfig.VERSION_NAME;
        String actual = Strings.getFormattedFileName(sess, ph);

        assertThat("Custom file name should do substitutions", actual, is(expected));


    }

    @Test
    public void getFormattedFileName_shouldPrefixSerialToFileName(){
        Session sess = mock(Session.class);
        when(sess.getCurrentFileName()).thenReturn("somefile");
        PreferenceHelper ph = mock(PreferenceHelper.class);
        when(ph.shouldPrefixSerialToFileName()).thenReturn(true);

        String expected = Build.SERIAL + "_somefile";
        String actual = Strings.getFormattedFileName(sess,ph);

        assertThat("Build serial is prefixed to file name", actual, is(expected));

    }


    @Test
    public void getFormattedFileName_shouldPrefixSerialToFileName_SerialOnlyIncludedOnce(){
        Session sess = mock(Session.class);
        when(sess.getCurrentFileName()).thenReturn(Build.SERIAL + "___somefile");
        PreferenceHelper ph = mock(PreferenceHelper.class);
        when(ph.shouldPrefixSerialToFileName()).thenReturn(true);

        String expected = Build.SERIAL + "___somefile";
        String actual = Strings.getFormattedFileName(sess, ph);
        assertThat("Build serial only included once", actual, is(expected));

    }

}
