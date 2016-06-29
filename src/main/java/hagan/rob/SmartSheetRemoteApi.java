package hagan.rob;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.*;
import com.smartsheet.api.oauth.Token;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;

public class SmartSheetRemoteApi
{

    private static final String WORKSPACE_NAME = "[210] FC Project Plans";
    private static final String COLUMN_TITLE = "Admin comment 1:";
    private static final String DATE;

    static
    {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern( "dd-MMM-uuuu hh:mm a" );
        DATE = dateTimeFormatter.format( LocalDateTime.now() );
    }

    public static void main( String[] args ) throws SmartsheetException
    {
        System.out.println( "DATE = " + DATE );

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

            final HashSet<Integer> rowNumbers = new HashSet<Integer>();
            rowNumbers.add( 1 );
            final Sheet sheet = smartsheet.sheetResources().getSheet( sheetSummary.getId(),
                                                                      null,
                                                                      null,
                                                                      null,
                                                                      rowNumbers,
                                                                      null,
                                                                      null,
                                                                      null );
            final List<Column> columns = sheet.getColumns();
            for ( final Column column : columns )
            {
                final String title = column.getTitle();
                if ( COLUMN_TITLE.equals( title ) )
                {
                    final Long adminCommentColumnId = column.getId();

                    final Row row = sheet.getRowByRowNumber( 1 );
                    final List<Cell> cells = row.getCells();
                    for ( final Cell cell : cells )
                    {
                        if ( cell.getColumnId().equals( adminCommentColumnId ) )
                        {
                            System.out.println( "*** found admin comment column = '" + cell.getValue() + "'" );

                            // To update cells, update the row containing the cell.
//                            cell.setValue( "Hide all columns from here on ... Automatic update: " + DATE );
                        }
                    }
                }
            }
        }
    }
}
