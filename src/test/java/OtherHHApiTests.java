import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

/**
 * @author iovchinnikova
 * @since  2015-11-30
 */
public class OtherHHApiTests {

    protected HttpClient client;
    protected JSONParser parser;

    private String getItemIdByName( String dictionaryName, String itemName) {
        return "<Item Id>";
    } //TODO: implement this method!
    private Object executeRequest(String url) throws Exception {

        HttpUriRequest request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

        HttpEntity entity = response.getEntity();
        Assert.assertNotNull(entity, "Invalid response.");
        Assert.assertEquals(ContentType.get(entity).getMimeType(), ContentType.APPLICATION_JSON.toString().split(";")[0]);

        String jsonText = EntityUtils.toString(entity, ContentType.get(entity).getCharset());

        return parser.parse(jsonText);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        client = new DefaultHttpClient();
        parser = new JSONParser();
    }
    @AfterMethod
    public void tearDown() {
        client.getConnectionManager().closeExpiredConnections();
    }

    //Checks the number of root elements in the dictionary. ------------------------------------------------------------
    /**
     * FORMAT: { testCaseName, baseUrl, resource, expectedDictionaryRootElementsNumber }
     * The testCaseName, baseUrl, resource and expectedDictionaryRootElementsNumber are required.
     * @return the test data for the 'checkRootElementsNumberInDictionary' test method.
     */
    @DataProvider(name = "TestDataSet1")
    public Object[][] createTestDataSet1() {
        return new Object[][]{

                { "TC001_CheckCountriesNumberInAreas", "https://api.hh.ru/", "areas",  9 },
                { "TC002_CheckCitiesNumberInMetro",    "https://api.hh.ru/", "metro", 12 },
                //etc
        };
    }
    /**
     * Checks the number of root elements in the dictionary.
     * @param testCaseName a name of the test case
     * @param baseUrl a base url
     * @param resource a page of the resource
     * @param expectedDictionaryRootElementsNumber an expected number of the root elements in the dictionary
     * @throws Exception
     */
    @Test(dataProvider = "TestDataSet1", enabled = true)
    public void checkRootElementsNumberInDictionary(String testCaseName, String baseUrl, String resource, int expectedDictionaryRootElementsNumber) throws Exception {

        Assert.assertFalse(StringUtils.isBlank(testCaseName), "Invalid parameter: testCaseName is null or empty.");
        System.out.println("TestCase: [" + testCaseName + "] is started.");

        Assert.assertFalse(StringUtils.isBlank(baseUrl), "Invalid parameter: baseUrl is null or empty.");
        Assert.assertFalse(StringUtils.isBlank(resource), "Invalid parameter: resource is null or empty.");

        String testedUrl = baseUrl+resource;
        JSONArray jsonResultArray = (JSONArray)executeRequest(testedUrl);

        Assert.assertEquals(jsonResultArray.size(), expectedDictionaryRootElementsNumber, MessageFormat.format(
                "The number of root elements in the dictionary [{0}] does not coincide with the expected value.", resource));

        System.out.println(MessageFormat.format(
                "The number of root elements in the dictionary [{0}] coincides with the expected value and is equal to {1}.",
                resource, expectedDictionaryRootElementsNumber));
        System.out.println("TestCase: [" + testCaseName + "] is successfully finished.");
    }

    //Checks an availability of the employer by the partial/full name in the region. -----------------------------------
    /**
     * FORMAT: { testCaseName, baseUrl, resource, partialEmployerName, fullEmployerName, region }
     * The testCaseName, baseUrl, resource, partialEmployerName and fullEmployerName are required; the region is optional.
     * Note that the employerName can be partial.
     * Default region value for 'api.hh.ru' host is 'Russia'.
     * @return the test data for the 'checkEmployerAvailabilityByNameInRegion' test method.
     */
    @DataProvider(name = "TestDataSet2")
    public Object[][] createTestDataSet2() {
        return new Object[][]{

                { "TC001", "https://api.hh.ru/", "employers", "Новые Облачные Технологии", "Новые Облачные Технологии", null     },
                { "TC002", "https://api.hh.ru/", "employers", "новые облачные",            "Новые Облачные Технологии", "  "     },
                { "TC002", "https://api.hh.ru/", "employers", "облачные",                  "Новые Облачные Технологии", ""       },
                { "TC003", "https://api.hh.ru/", "employers", "новые облачные",            "Новые Облачные Технологии", "Россия" },
                //etc
        };
    }
    /**
     * Checks an availability of the employer by the partial/full name in the region.
     * @param testCaseName a name of the test case
     * @param baseUrl a base url
     * @param resource a page of the resource
     * @param partialEmployerName a partial name of the employer
     * @param fullEmployerName a full name of the employer; used as a template for search that particular name of the employer
     * @param region a name of the region where the employer is checked
     * @throws Exception
     */
    @Test(dataProvider = "TestDataSet2", enabled = true)
    public void checkEmployerAvailabilityByNameInRegion(String testCaseName, String baseUrl, String resource, String partialEmployerName, String fullEmployerName, String region) throws Exception {

        Assert.assertFalse(StringUtils.isBlank(testCaseName), "Invalid parameter: testCaseName is null or empty.");
        System.out.println("TestCase: [" + testCaseName + "] is started.");

        Assert.assertFalse(StringUtils.isBlank(baseUrl), "Invalid parameter: baseUrl is null or empty.");
        Assert.assertFalse(StringUtils.isBlank(resource), "Invalid parameter: resource is null or empty.");
        Assert.assertFalse(StringUtils.isBlank(partialEmployerName), "Invalid parameter: partialEmployerName is null or empty.");
        Assert.assertFalse(StringUtils.isBlank(fullEmployerName), "Invalid parameter: fullEmployerName is null or empty.");

        String testedUrl = baseUrl+resource + "?text=" + partialEmployerName;
        if (StringUtils.isNotBlank(region)) {
            testedUrl += "&area=113"; //testedUrl += "&area=" + getItemIdByName("areas", "<RegionName>"); //TODO: use the getItemIdByName method!
        } else {
            testedUrl += "&area=113"; //testedUrl += "&area=" + getItemIdByName("areas", "Россия"); //TODO: use the getItemIdByName method!
        }
        testedUrl = testedUrl.replace(" ", "%20");
        testedUrl = testedUrl.replace("\"", "%22");

        JSONObject jsonResultObject = (JSONObject)executeRequest(testedUrl);

        String numberFoundEmployers = jsonResultObject.get("found").toString();
        Assert.assertFalse(numberFoundEmployers.equals("0"), MessageFormat.format("No one employer with the partial name {0} is found.", partialEmployerName));

        int actualEmployersNumber = 0;

        for (Object item : (JSONArray)jsonResultObject.get("items")) {
            JSONObject element = (JSONObject) item;

            String actualEmployerName = element.get("name").toString();
            if (actualEmployerName.equals(fullEmployerName)) {
                actualEmployersNumber++;
            }
        }

        Assert.assertEquals(actualEmployersNumber, 1, "The number of employers does not coincide with the expected value.");

        String result;
        if (StringUtils.isNotBlank(region)) {
            result = MessageFormat.format(
                    "The employer with that particular name [{0}] in the region [{1}] is found by the partial name [{2}].",
                    fullEmployerName, region, partialEmployerName);
        } else {

            result = MessageFormat.format(
                    "The employer with that particular name [{0}] is found by the partial name [{1}].",
                    fullEmployerName, partialEmployerName);
        }
        System.out.println(result);
        System.out.println("TestCase: [" + testCaseName + "] is successfully finished.");
    }

    //Checks an availability of the vacancy by the full name in the region. --------------------------------------------
    /**
     * FORMAT: { testCaseName, baseUrl, resource, vacancyFullName, employerName, region }
     * The testCaseName, baseUrl, resource and vacancyFullName are required; the employerName and region are optional.
     * Note that the employerName can be partial.
     * Default region value for 'https://api.hh.ru' host is 'Russia'.
     * @return the test data for the 'checkVacancyAvailabilityByFullName' test method.
     */
    @DataProvider(name = "TestDataSet3")
    public Object[][] createTestDataSet3() {
        return new Object[][] {

                { "TC001", "https://api.hh.ru/", "vacancies", "QA Automation Engineer", null,                        null              },
                { "TC002", "https://api.hh.ru/", "vacancies", "QA Automation Engineer", null,                        ""                },
                { "TC003", "https://api.hh.ru/", "vacancies", "QA Automation Engineer", "",                          null              },
                { "TC004", "https://api.hh.ru/", "vacancies", "QA Automation Engineer", "    ",                      "     "           },
                { "TC005", "https://api.hh.ru/", "vacancies", "QA Automation Engineer", null,                        "Санкт-Петербург" },
                { "TC006", "https://api.hh.ru/", "vacancies", "QA Automation Engineer", "Новые Облачные Технологии", null              },
                { "TC007", "https://api.hh.ru/", "vacancies", "QA Automation Engineer", "новые облачные",            null              },
                { "TC008", "https://api.hh.ru/", "vacancies", "QA Automation Engineer", "Новые Облачные Технологии", "Санкт-Петербург" },
                { "TC009", "https://api.hh.ru/", "vacancies", "QA Automation Engineer", "новые облачные",            "Санкт-Петербург" },
                //etc
        };
    }
    /**
     * Checks an availability of the vacancy by the full name in the region.
     * @param testCaseName a name of the test case
     * @param baseUrl a base url
     * @param resource a page of the resource
     * @param vacancyFullName a full name of the checked vacancy
     * @param employerName a name of the employer
     * @param region a name of the region where the vacancy is checked
     * @throws Exception
     */
    @Test(dataProvider = "TestDataSet3", enabled = true)
    public void checkVacancyAvailabilityByFullName(String testCaseName, String baseUrl, String resource, String vacancyFullName, String employerName, String region) throws Exception {

        Assert.assertFalse(StringUtils.isBlank(testCaseName), "Invalid parameter: testCaseName is null or empty.");
        System.out.println("TestCase: [" + testCaseName + "] is started.");

        Assert.assertFalse(StringUtils.isBlank(baseUrl), "Invalid parameter: baseUrl is null or empty.");
        Assert.assertFalse(StringUtils.isBlank(resource), "Invalid parameter: resource is null or empty.");
        Assert.assertFalse(StringUtils.isBlank(vacancyFullName), "Invalid parameter: vacancyName is null or empty.");

        String testedUrl = baseUrl+resource + "?text=!\"" + vacancyFullName + "\"";
        if (StringUtils.isNotBlank(employerName)) {
            testedUrl += "&employer_id=213397"; //testedUrl += "&employer_id=" + getItemIdByName("employers", "<Employer Name>"); //TODO: use the getItemIdByName method!
        }
        if (StringUtils.isNotBlank(region)) {
            testedUrl += "&area=2"; //testedUrl += "&area=" + getItemIdByName("areas", "<RegionName>"); //TODO: use the getItemIdByName method!
        } else {
            testedUrl += "&area=113"; //testedUrl += "&area=" + getItemIdByName("areas", "Россия"); //TODO: use the getItemIdByName method!
        }
        testedUrl = testedUrl.replace(" ", "%20");
        testedUrl = testedUrl.replace("\"", "%22");

        JSONObject jsonResultObject = (JSONObject)executeRequest(testedUrl);
        String numberFoundVacancies = jsonResultObject.get("found").toString();
        Assert.assertFalse(numberFoundVacancies.equals("0"), MessageFormat.format("No one vacancy with name [{0}] is found.", vacancyFullName));

        Long pageCounter = (Long)jsonResultObject.get("pages");
        int vacancyCounter = 0;

        while (pageCounter > 0) {

            jsonResultObject = (JSONObject)executeRequest(testedUrl + "&page=" + (pageCounter - 1));

            for (Object item : (JSONArray)jsonResultObject.get("items")) {
                JSONObject element = (JSONObject)item;

                String actualVacancyName = element.get("name").toString();
                if (actualVacancyName.equals(vacancyFullName)) {
                    vacancyCounter++;
                }
            }
            pageCounter--;
        }

        Assert.assertNotEquals(vacancyCounter, 0, MessageFormat.format("No one vacancy with that particular name [{0}] is found.", vacancyFullName));

        String result;
        if (StringUtils.isNotBlank(employerName) && StringUtils.isNotBlank(region)) {
            result = MessageFormat.format(
                    "The vacancies with that particular name [{0}] from the employer [{1}] in the region [{2}] are found and their number is equal to {3}.",
                    vacancyFullName, employerName, region, vacancyCounter);
        } else if (StringUtils.isNotBlank(employerName) && StringUtils.isBlank(region)){
            result = MessageFormat.format(
                    "The vacancies with that particular name [{0}] from the employer [{1}] are found and their number is equal to {2}.",
                    vacancyFullName, employerName, vacancyCounter);
        } else if (StringUtils.isBlank(employerName) && StringUtils.isNotBlank(region)) {
            result = MessageFormat.format(
                    "The vacancies with that particular name [{0}] in the region [{1}] are found and their number is equal to {2}.",
                    vacancyFullName, region, vacancyCounter);
        } else {
            result = MessageFormat.format(
                    "The vacancies with that particular name [{0}] are found and their number is equal to {1}.",
                    vacancyFullName, vacancyCounter);
        }
        System.out.println(result);
        System.out.println("TestCase: [" + testCaseName + "] is successfully finished.");
    }
}
