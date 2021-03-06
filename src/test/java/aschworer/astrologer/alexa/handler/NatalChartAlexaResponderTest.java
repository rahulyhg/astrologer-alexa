package aschworer.astrologer.alexa.handler;

import aschworer.astrologer.alexa.handler.responder.service.Cards;
import aschworer.astrologer.alexa.handler.responder.service.NatalChartAlexaResponder;
import aschworer.astrologer.alexa.handler.responder.service.NatalChartIntent;
import aschworer.astrologer.alexa.service.model.Sign;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.SsmlOutputSpeech;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.ResourceBundle;

import static aschworer.astrologer.alexa.handler.responder.service.SessionConstants.*;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * @author aschworer
 */
@RunWith(MockitoJUnitRunner.class)
public class NatalChartAlexaResponderTest extends StandardAlexaResponderTest {

    private NatalChartAlexaResponder natalChartAlexaResponder = new NatalChartAlexaResponder();

    @Mock
    private Session session;

    @Before
    public void init() {
        ResourceBundle config = ResourceBundle.getBundle("lambda");
        System.setProperty("aws.accessKeyId", config.getString("aws.accessKeyId"));
        System.setProperty("aws.secretKey", config.getString("aws.secretKey"));
    }

    @Test
    public void testNatalChartIntent() throws Exception {
        final SpeechletResponse speechletResponse = natalChartAlexaResponder.respondToIntent(buildIntent(NatalChartIntent.NATAL_CHART_INTENT.getName()), session);
        assertEquals(Cards.TELL_ME_BIRTH_DAY, speechletResponse.getCard().getTitle());
        assertFalse(speechletResponse.getShouldEndSession());
        Mockito.verify(session).setAttribute(INITIAL_INTENT, NatalChartIntent.NATAL_CHART_INTENT.getName());
    }

    @Test
    public void testMoonSignIntent() throws Exception {
        final SpeechletResponse speechletResponse = natalChartAlexaResponder.respondToIntent(buildIntent(NatalChartIntent.MOON_SIGN_INTENT.getName()), session);
        assertEquals(Cards.TELL_ME_BIRTH_DAY, speechletResponse.getCard().getTitle());
        assertFalse(speechletResponse.getShouldEndSession());
        Mockito.verify(session).setAttribute(INITIAL_INTENT, NatalChartIntent.MOON_SIGN_INTENT.getName());
    }

    @Test
    public void testSunSignIntent() throws Exception {
        final SpeechletResponse speechletResponse = natalChartAlexaResponder.respondToIntent(buildIntent(NatalChartIntent.SUN_SIGN_INTENT.getName()), session);
        assertEquals(Cards.TELL_ME_BIRTH_DAY, speechletResponse.getCard().getTitle());
        assertFalse(speechletResponse.getShouldEndSession());
        Mockito.verify(session).setAttribute(INITIAL_INTENT, NatalChartIntent.SUN_SIGN_INTENT.getName());
    }

    @Test
    public void testBirthDateIntent() throws Exception {
        Mockito.when(session.getAttribute(BIRTH_DATE)).thenReturn("1985-11-20");
        final SpeechletResponse response = natalChartAlexaResponder.respondToIntent(buildIntentWithSlots(NatalChartIntent.BIRTH_DAY_INTENT.getName(), buildSlotsMap("day", "1985-11-20")), session);
        Mockito.verify(session).setAttribute(BIRTH_DATE, "1985-11-20");
        assertEquals(Cards.DOUBLE_CHECK_DATE, response.getCard().getTitle());
        assertFalse(response.getShouldEndSession());
    }

    @Test
    public void testBirthDateIntentYearMissing() throws Exception {
        Mockito.when(session.getAttribute(BIRTH_DATE)).thenReturn("2017-11-20");
        final SpeechletResponse response = natalChartAlexaResponder.respondToIntent(buildIntentWithSlots(NatalChartIntent.BIRTH_DAY_INTENT.getName(), buildSlotsMap("day", "2017-11-20")), session);
        Mockito.verify(session).setAttribute(BIRTH_DATE, "2017-11-20");
        assertEquals(Cards.TELL_ME_BIRTH_YEAR, response.getCard().getTitle());
        assertFalse(response.getShouldEndSession());
    }

    @Test
    public void testDenyDateIntent() throws Exception {
        final SpeechletResponse response = natalChartAlexaResponder.respondToIntent(Intent.builder().withName(NatalChartIntent.DENY_DATE_INTENT.getName()).build(), session);
        assertEquals(Cards.TELL_ME_BIRTH_DAY, response.getCard().getTitle());
        assertFalse(response.getShouldEndSession());
    }

    @Test
    public void testBirthYearIntent() throws Exception {
        Mockito.when(session.getAttribute(BIRTH_DATE)).thenReturn("2015-11-20");
        final SpeechletResponse response = natalChartAlexaResponder.respondToIntent(buildIntentWithSlots(NatalChartIntent.BIRTH_YEAR_INTENT.getName(), buildSlotsMap("year", "1986")), session);
        Mockito.verify(session).setAttribute(BIRTH_YEAR, "1986");
        assertEquals(Cards.DOUBLE_CHECK_DATE, response.getCard().getTitle());
        assertFalse(response.getShouldEndSession());
    }

    @Test
    public void testConfirmBirthDateIntent_OnSunSign_Success() throws Exception {
        Mockito.when(session.getAttribute(INITIAL_INTENT)).thenReturn(NatalChartIntent.SUN_SIGN_INTENT.getName());
        Mockito.when(session.getAttribute("date")).thenReturn("1985-11-29");
        final SpeechletResponse response = natalChartAlexaResponder.respondToIntent(Intent.builder().withName(NatalChartIntent.CONFIRM_DATE_INTENT.getName()).build(), session);
        assertEquals(Cards.SPEAK_PLANET_SIGN, response.getCard().getTitle());
        assertTrue(response.getShouldEndSession());
        assertResponseMentionSigns(response);
        assertFalse(((SsmlOutputSpeech) response.getOutputSpeech()).getSsml().toLowerCase().contains("1985"));
    }

    @Test
    public void testConfirmBirthDateIntent_OnSunSign_NoYear() throws Exception {
        Mockito.when(session.getAttribute(INITIAL_INTENT)).thenReturn(NatalChartIntent.SUN_SIGN_INTENT.getName());
        Mockito.when(session.getAttribute("date")).thenReturn(CURRENT_YEAR + "-11-29");
        final SpeechletResponse response = natalChartAlexaResponder.respondToIntent(Intent.builder().withName(NatalChartIntent.CONFIRM_DATE_INTENT.getName()).build(), session);
        assertEquals(Cards.SPEAK_PLANET_SIGN, response.getCard().getTitle());
        assertTrue(response.getShouldEndSession());
        assertResponseMentionSigns(response);
        assertFalse(((SsmlOutputSpeech) response.getOutputSpeech()).getSsml().toLowerCase().contains(CURRENT_YEAR));
    }

    @Test
    public void testBirthPlaceIntentSuccess() throws Exception {
        Mockito.when(session.getAttribute(INITIAL_INTENT)).thenReturn(NatalChartIntent.NATAL_CHART_INTENT.getName());
        Mockito.when(session.getAttribute("date")).thenReturn("1985-11-20");
        final String country = "Australia";
        Mockito.when(session.getAttribute("place")).thenReturn(country);
        Mockito.when(session.getAttribute("place_full")).thenReturn(country);
        Mockito.when(session.getAttribute("lat")).thenReturn("+33.33");
        Mockito.when(session.getAttribute("lng")).thenReturn("-33.33");
//        Mockito.when(session.getAttribute("date")).thenReturn("1985-11-20");//time too
        final SpeechletResponse response = natalChartAlexaResponder.respondToIntent(buildIntentWithSlots(NatalChartIntent.BIRTH_PLACE_INTENT.getName(), buildSlotsMap("place", country)), session);
        assertEquals(Cards.SPEAK_NATAL_CHART, response.getCard().getTitle());
        assertTrue(response.getShouldEndSession());
        assertResponseMentions(response, "in australia");
        assertResponseMentions(response, "1985-11-20");
    }

    @Test
    public void testNatalChartSuccess() throws Exception {
        Mockito.when(session.getAttribute(INITIAL_INTENT)).thenReturn(NatalChartIntent.NATAL_CHART_INTENT.getName());
        Mockito.when(session.getAttribute("date")).thenReturn("1986-04-20");
        Mockito.when(session.getAttribute("place")).thenReturn("some place");
        Mockito.when(session.getAttribute("lat")).thenReturn("+33.33");
        Mockito.when(session.getAttribute("lng")).thenReturn("-33.33");
        final SpeechletResponse response = natalChartAlexaResponder.respondToIntent(Intent.builder().withName(NatalChartIntent.CONFIRM_DATE_INTENT.getName()).build(), session);
        assertEquals(Cards.SPEAK_NATAL_CHART, response.getCard().getTitle());
        assertTrue(response.getShouldEndSession());
        assertResponseMentionSigns(response);
    }


    @Test
    @Ignore
    public void testNatalChartSuccessBirthPlaceRequired() throws Exception {
        Mockito.when(session.getAttribute(INITIAL_INTENT)).thenReturn(NatalChartIntent.NATAL_CHART_INTENT.getName());
        Mockito.when(session.getAttribute("date")).thenReturn("1986-04-20");
        Mockito.when(session.getAttribute("place")).thenReturn("some place");
        Mockito.when(session.getAttribute("lat")).thenReturn(null);
        Mockito.when(session.getAttribute("lng")).thenReturn("-33.33");
        final SpeechletResponse response = natalChartAlexaResponder.respondToIntent(Intent.builder().withName(NatalChartIntent.CONFIRM_DATE_INTENT.getName()).build(), session);
        assertEquals(Cards.TELL_ME_BIRTH_PLACE, response.getCard().getTitle());
        assertFalse(response.getShouldEndSession());
    }

    @Test
    public void testMoonSignSuccess() throws Exception {
        Mockito.when(session.getAttribute(INITIAL_INTENT)).thenReturn(NatalChartIntent.MOON_SIGN_INTENT.getName());
        Mockito.when(session.getAttribute("date")).thenReturn("1986-04-20");
        Mockito.when(session.getAttribute("place")).thenReturn("some place");
        Mockito.when(session.getAttribute("lat")).thenReturn("+33.33");
        Mockito.when(session.getAttribute("lng")).thenReturn("-33.33");
        final SpeechletResponse response = natalChartAlexaResponder.respondToIntent(Intent.builder().withName(NatalChartIntent.CONFIRM_DATE_INTENT.getName()).build(), session);
        assertEquals(Cards.SPEAK_PLANET_SIGN, response.getCard().getTitle());
        assertTrue(response.getShouldEndSession());
        assertResponseMentionSigns(response);
    }

    @Test
    @Ignore
    public void test_MoonSignSuccessBirthPlaceRequired() throws Exception {
        Mockito.when(session.getAttribute(INITIAL_INTENT)).thenReturn(NatalChartIntent.MOON_SIGN_INTENT.getName());
        Mockito.when(session.getAttribute("date")).thenReturn("1986-04-20");
        Mockito.when(session.getAttribute("place")).thenReturn("some place");
        Mockito.when(session.getAttribute("lat")).thenReturn("+33.33");
        Mockito.when(session.getAttribute("lng")).thenReturn(null);
        final SpeechletResponse response = natalChartAlexaResponder.respondToIntent(Intent.builder().withName(NatalChartIntent.CONFIRM_DATE_INTENT.getName()).build(), session);
        assertEquals(Cards.TELL_ME_BIRTH_PLACE, response.getCard().getTitle());
        assertFalse(response.getShouldEndSession());
    }

    @Test
    @Ignore
    public void testConfirmBirthDateIntentOnNatalChartBirthTimeRequired() throws Exception {
        Mockito.when(session.getAttribute(INITIAL_INTENT)).thenReturn(NatalChartIntent.NATAL_CHART_INTENT.getName());
        assertEquals(Cards.TELL_ME_BIRTH_TIME, natalChartAlexaResponder.respondToIntent(
                Intent.builder().withName(NatalChartIntent.BIRTH_DAY_INTENT.toString()).withSlots(buildSlotsMap("day", "20-11-1985")).build(),
                session).getCard().getTitle());
    }

    @Test
    @Ignore
    public void testBirthTimeIntentOnMoonSign() throws Exception {
        Mockito.when(session.getAttribute(INITIAL_INTENT)).thenReturn(NatalChartIntent.MOON_SIGN_INTENT.getName());
        assertEquals(Cards.SPEAK_PLANET_SIGN, natalChartAlexaResponder.respondToIntent(
                Intent.builder().withName(NatalChartIntent.BIRTH_TIME_INTENT.toString()).withSlots(buildSlotsMap("time", "07:23")).build(),
                session).getCard().getTitle());
    }

    @Test
    @Ignore
    public void testBirthTimeIntentOnNatalChartBirthPlaceRequired() throws Exception {
        Mockito.when(session.getAttribute(INITIAL_INTENT)).thenReturn(NatalChartIntent.NATAL_CHART_INTENT.getName());
        assertEquals(Cards.TELL_ME_BIRTH_PLACE, natalChartAlexaResponder.respondToIntent(
                Intent.builder().withName(NatalChartIntent.BIRTH_TIME_INTENT.toString()).withSlots(buildSlotsMap("time", "07:23")).build(),
                session).getCard().getTitle());
    }


    private HashMap<String, Slot> buildSlotsMap(String name, String value) {
        HashMap<String, Slot> slots = new HashMap<>();
        slots.put(name, Slot.builder()
                .withName(name)
                .withValue(value).build());
        return slots;
    }

    private Intent buildIntentWithSlots(String intentName, HashMap<String, Slot> slots) {
        return Intent.builder().withName(intentName).withSlots(slots).build();
    }

    private void assertResponseMentionSigns(SpeechletResponse response) {
        assertThat(((SsmlOutputSpeech) response.getOutputSpeech()).getSsml().toLowerCase(), CoreMatchers.anyOf(
                containsString(Sign.AQUARIUS.toString().toLowerCase()),
                containsString(Sign.ARIES.toString().toLowerCase()),
                containsString(Sign.CANCER.toString().toLowerCase()),
                containsString(Sign.CAPRICORN.toString().toLowerCase()),
                containsString(Sign.GEMINI.toString().toLowerCase()),
                containsString(Sign.LEO.toString().toLowerCase()),
                containsString(Sign.LIBRA.toString().toLowerCase()),
                containsString(Sign.PISCES.toString().toLowerCase()),
                containsString(Sign.SAGITTARIUS.toString().toLowerCase()),
                containsString(Sign.SCORPIO.toString().toLowerCase()),
                containsString(Sign.TAURUS.toString().toLowerCase()),
                containsString(Sign.VIRGO.toString().toLowerCase()),
                containsString(Sign.VARY.toString().toLowerCase())
        ));
    }

    private void assertResponseMentions(SpeechletResponse response, String string) {
        assertTrue(((SsmlOutputSpeech) response.getOutputSpeech()).getSsml().toLowerCase().contains(string));
    }

    private Intent buildIntent(String intentName) {
        return Intent.builder().withName(intentName).build();
    }

}
