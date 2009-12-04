package eu.europeana.web.util;

import eu.europeana.database.domain.Token;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.ui.rememberme.PersistentRememberMeToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

/**
 * Test the token repository
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Vitali Kiruta
 * @author Sjoerd Siebinga  <sjoerd.siebinga@gmail.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/core-application-context.xml"})
public class TestTokens {
//    private static final Logger log = Logger.getLogger(TestUtilities.class);

    @Autowired
    private TokenRepositoryService tokenRepositoryService;

    @Autowired
    private TokenService tokenService;

    @Test
    public void testToken() {
        Date now = new Date();
        PersistentRememberMeToken t = new PersistentRememberMeToken(
                "user1", "series1", "token1", now
        );
        tokenRepositoryService.createNewToken(t);
        PersistentRememberMeToken t2 = tokenRepositoryService.getTokenForSeries("series1");
        assertEquals(t.getSeries(), t2.getSeries());
        assertEquals(t.getTokenValue(), t2.getTokenValue());
        assertEquals(t.getUsername(), t2.getUsername());
    }

    @Test
    public void createToken() {
        tokenService.createNewToken("test@example.com");
        Token token = tokenService.getTokenByEmail("test@example.com");
        assertNotNull(token);
        tokenService.removeToken(token);
        long tokenCount = tokenService.countTokens();
        assertEquals(0, tokenCount);
    }
}