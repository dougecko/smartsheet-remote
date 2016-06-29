package hagan.rob;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.*;
import com.smartsheet.api.oauth.Token;

import java.util.List;

public class SmartSheetRemoteApi
{

    private static final String WORKSPACE_NAME = "[210] FC Project Plans";

    public static void main( String[] args ) throws SmartsheetException
    {
        // Set the Access Token
        final Token token = new Token();
        token.setAccessToken( args[0] );

        // Use the Smartsheet Builder to create an instance of Smartsheet
        final Smartsheet smartsheet = new SmartsheetBuilder().setAccessToken( token.getAccessToken() ).build();

        final PaginationParameters.PaginationParametersBuilder paginationParametersBuilder =
                new PaginationParameters.PaginationParametersBuilder();
        paginationParametersBuilder.setIncludeAll( true );
        final PaginationParameters paginationParameters = paginationParametersBuilder.build();
        final PagedResult<Workspace> workspacePagedResult =
                smartsheet.workspaceResources().listWorkspaces( paginationParameters );
        final List<Workspace> workspaces = workspacePagedResult.getData();

        for ( final Workspace workspaceSummary : workspaces )
        {
            final String workspaceName = workspaceSummary.getName();
            if ( !(WORKSPACE_NAME.equals( workspaceName )) )
            {
                continue;
            }

            System.out.println( "workspace = " + workspaceName );

            final Long workspaceId = workspaceSummary.getId();

            final Workspace workspace = smartsheet.workspaceResources().getWorkspace( workspaceId, null, null );
            final List<Folder> folders = workspace.getFolders();
            if ( null == folders )
            {
                continue;
            }

            for ( final Folder folderSummary : folders )
            {
                System.out.println( "   folder = " + folderSummary.getName() );

                final Long folderId = folderSummary.getId();

                final Folder folder = smartsheet.folderResources().getFolder( folderId, null );

                doFolderSubFolders( smartsheet, folder );
                doFolderSheets( smartsheet, folder );
            }
        }
    }

    private static void doFolderSubFolders( Smartsheet smartsheet, Folder folder ) throws SmartsheetException
    {
        final List<Folder> subFolders = folder.getFolders();
        if ( null == subFolders )
        {
            return;
        }

        for ( final Folder subFolderSummary : subFolders )
        {
            final Folder subFolder = smartsheet.folderResources().getFolder( subFolderSummary.getId(), null );
            System.out.println( "      subFolder = " + subFolder.getName() );

            doFolderSheets( smartsheet, subFolder );
        }
    }

    private static void doFolderSheets( Smartsheet smartsheet, Folder folder ) throws SmartsheetException
    {
        final List<Sheet> sheets = folder.getSheets();
        if ( null == sheets )
        {
            return;
        }

        for ( final Sheet sheetSummary : sheets )
        {
            System.out.println( "      sheet = " + sheetSummary.getName() );

            final Sheet sheet = smartsheet.sheetResources().getSheet( sheetSummary.getId(),
                                                                      null,
                                                                      null,
                                                                      null,
                                                                      null,
                                                                      null,
                                                                      null,
                                                                      null );

            System.out.println( "*** sheet name = " + sheet.getName() );
        }
    }
}
