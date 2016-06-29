package hagan.rob;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.StringReader;

public class SmartSheetRemote
{
    private static final int CONNECT_TIMEOUT_IN_MILLIS = 2000;
    private static final int READ_TIMEOUT_IN_MILLIS = 10000;

    public static void main( String[] args )
    {
        final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<String, Object>();
        headers.putSingle( "Authorisation: Bearer", args[0] );
        final JsonObject jsonObject = getJsonObject( "https://api.smartsheet.com/2.0/workspaces", headers );

        final int id = jsonObject.getInt( "id" );
        System.out.println( "id = " + id );

        final String name = jsonObject.getString( "name" );
        System.out.println( "name = " + name );
    }

    private static Client newClient()
    {
        return ClientBuilder.newClient().
                property( "jersey.config.client.connectTimeout", CONNECT_TIMEOUT_IN_MILLIS ).
                property( "jersey.config.client.readTimeout", READ_TIMEOUT_IN_MILLIS );
    }

    private static Invocation.Builder buildRequest( final String url,
                                                    final MultivaluedMap<String, Object> headers )
    {
        return newClient().target( url ).request( MediaType.APPLICATION_JSON ).headers( headers );
    }

    public static JsonObject getJsonObject( final String url,
                                            final MultivaluedMap<String, Object> headers )
    {
        try
        {
            final Invocation.Builder request = buildRequest( url, headers );
            final Response response = request.get();

            final Response.StatusType statusInfo = response.getStatusInfo();
            if ( statusInfo.getFamily() != Response.Status.Family.SUCCESSFUL )
            {
                throw new RuntimeException(
                        "Call returned status " + statusInfo.getStatusCode() + " - " + statusInfo.getReasonPhrase() );
            }
            return Json.createReader( new StringReader( response.readEntity( String.class ) ) ).readObject();
        }
        catch ( final RuntimeException e )
        {
            throw new RuntimeException( "Request failed: " + url, e );
        }
    }
}
