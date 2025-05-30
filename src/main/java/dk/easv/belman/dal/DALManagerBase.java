package dk.easv.belman.dal;

import dk.easv.belman.exceptions.BelmanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DALManagerBase {
    protected static final ConnectionManager connectionManager = new ConnectionManager();
    protected static final Logger logger = LoggerFactory.getLogger(DALManagerBase.class);

    protected static final String USER_LAST_LOGIN_TIME = "last_login_time";
    protected static final String ID =  "id";
    protected static final String USER_FULL_NAME =  "full_name";
    protected static final String USER_USERNAME =  "username";
    protected static final String USER_PASSWORD =  "password";
    protected static final String USER_TAG_ID =  "tag_id";
    protected static final String USER_ROLE_ID =  "role_id";
    protected static final String USER_IS_ACTIVE =   "is_active";
    protected static final String USER_CREATED_AT =   "created_at";
    protected static final String PHOTOS_ANGLE =   "angle";
    protected static final String IS_DELETED =    "is_deleted";
    protected static final String DELETED_BY =    "deleted_by";
    protected static final String DELETED_AT =    "deleted_at";
    protected static final String UPLOADED_BY =  "uploaded_by";
    protected static final String UPLOADED_AT =  "uploaded_at";

    protected DALManagerBase() throws BelmanException {}

}
