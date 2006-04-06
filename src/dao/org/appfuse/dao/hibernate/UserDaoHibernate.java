package org.appfuse.dao.hibernate;

import java.util.List;

import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.appfuse.dao.UserDao;
import org.appfuse.model.User;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * This class interacts with Spring's HibernateTemplate to save/delete and
 * retrieve User objects.
 *
 * <p>
 * <a href="UserDaoHibernate.java.html"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 *   Modified by <a href="mailto:dan@getrolling.com">Dan Kibler</a>
 *   Extended to implement Acegi UserDetailsService interface by David Carter david@carter.net
*/
public class UserDaoHibernate extends BaseDaoHibernate implements UserDao, UserDetailsService {
    /**
     * @see org.appfuse.dao.UserDao#getUser(java.lang.String)
     */
    public User getUser(String username) {
        User user = (User) getHibernateTemplate().get(User.class, username);

        if (user == null) {
            log.warn("uh oh, user '" + username + "' not found...");
            throw new ObjectRetrievalFailureException(User.class, username);
        }

        return user;
    }

    /**
     * @see org.appfuse.dao.UserDao#getUsers(org.appfuse.model.User)
     */
    public List getUsers(User user) {
        return getHibernateTemplate().find("from User u order by upper(u.username)");
    }

    /**
     * @see org.appfuse.dao.UserDao#saveUser(org.appfuse.model.User)
     */
    public void saveUser(final User user) {
        if (log.isDebugEnabled()) {
            log.debug("user's id: " + user.getUsername());
        }
        
        getHibernateTemplate().saveOrUpdate(user);
        // necessary to throw a DataIntegrityViolation and catch it in UserManager
        getHibernateTemplate().flush();
    }

    /**
     * @see org.appfuse.dao.UserDao#removeUser(java.lang.String)
     */
    public void removeUser(String username) {
        getHibernateTemplate().delete(getUser(username));
    }

    /** 
    * @see org.acegisecurity.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
    */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return getUser(username);
        } catch (ObjectRetrievalFailureException e) {
            throw new UsernameNotFoundException("user '" + username + "' not found...");
        }
    }
}