The `ereefs-database` library is used to simplify interaction with the database.

It's divided in 3 groups, with different level of abstraction.

- `database`: Low level objects used to select, insert, update and delete
    JSON documents from the database. It has a builtin cache that can be enabled
    to cache JSON documents in memory or on disk, to reduce load on the Database
    when the project ask for a document multiple times.


- `bean`: Simple bean objects representing an entity saved in the Database.
    The beans are used to parse the JSON documents returned by the database,
    and help the programmer to know what to expect from the entities. They can
    also be used to modify or create a new entity to be saved in the database.


- `helper`: The helpers are high level objects. They merge the low level
    database objects with the beans, to completely abstract the database
    implementation. The helpers make common operations easier to perform.
    For example, they can be used to iterate through all
    the document in the database, or update a document without having to
    deal with JSON documents.


## Package database

The package is divided in 2 sub-packages:

- `table`: Abstract represent a database table, also known as a `collection`
    in document databases.

- `manager`: Used to perform common database operations on specific tables.

### Package database.table

This package can be used to represent any database table (aka `collection`).
It is responsible for:

- Handling JSON document cache,
- Handling database connection with retries when the connection fails,
- Check if a table exists in the database,
- Check if a document with a specific primary key exists in the database,
- Select, insert, update and delete JSON documents.

### Package database.manager

Managers use `database.table` for a specific table found in the database.

Available managers are:

- `DownloadManager`: Used to manage download definitions used by the
    `ereefs-download-manager` project,


- `MetadataManager`: Used to manage metadata documents for NetCDF files or
    NcAnimate output files,


- `ProductManager`: Used to manage NcAnimate and NcAggregate product definitions.


- `TaskManager`: Used to manage system tasks. See the `TaskManager` documentation
    for more information.


- `ncanimate.ConfigManager` and `ncanimate.ConfigPartManager`: Used to retrieve
    NcAnimate configuration from the database.


## Package bean

Beans are grouped in sub-packages depending on their usage.

- `download`: Bean used by the Download Manager.

- `metadata`: Bean used to represent a NetCDF file metadata
    or NcAnimate output file metadata.

- `ncanimate`: Bean used to represent a NcAnimate configuration
    document, or any of the configuration fragments (config parts).

The package also contains the `NetCDFUtils` class, used
to scan a NetCDF file for corruption using the DataScanner,
and method to calculate the min and max value of a variable.

To extract the metadata from a NetCDF file, use `NetCDFMetadataBean.create(...)`.

## Package helper

List of helper:

- `DownloadHelper`: Used by the Download Manager to list the
    download definitions.

- `MetadataHelper`: Used to select or delete NcAnimate output file metadata,
    and select NetCDF file metadata. It can also be used to download a NetCDF
    file associated with a NetCDF file metadata.

- `NcAnimateConfigHelper`: Used to retrieve NcAnimate configuration
    from the database. The helper take care of assembling the configuration
    by combining all the referred configuration fragments (config parts).
    It also has a helper function which can be used to find all the
    NetCDF files a NcAnimate configuration may require as input files,
    and other similar helper functions.

- `TaskHelper`: Used to retrieve a task from the database.

- `TestHelper`: Used to setup the in-memory database before running unit tests.


## Other useful classes

### DataScanner

Class used to scan the data of a NetCDF file for corruption.

### ZipUtils

Class used to recognise zip archive and unzip them.