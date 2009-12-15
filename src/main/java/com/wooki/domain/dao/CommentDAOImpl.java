package com.wooki.domain.dao;

import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.wooki.domain.model.Comment;
import com.wooki.domain.model.CommentState;

@Repository("commentDao")
public class CommentDAOImpl extends GenericDAOImpl<Comment, Long> implements
		CommentDAO {

	private Logger logger = LoggerFactory.getLogger(ActivityDAO.class);

	public boolean isOwner(Long commId, String username) {
		if (commId == null) {
			throw new IllegalArgumentException("Comment id cannot be null");
		}
		Query query = this.entityManager.createQuery("select count(c) from "
				+ this.getEntityType()
				+ " c join c.user as u where c.id=:id and u.username=:un and c.deletionDate is null");
		Long result = (Long) query.setParameter("un", username).setParameter(
				"id", commId).getSingleResult();
		return result > 0;
	}

	public List<Comment> listForPublication(Long chapterId) {
		if (chapterId == null) {
			throw new IllegalArgumentException("Chapter id cannot be null.");
		}
		Query query = this.entityManager.createQuery("from " + getEntityType()
				+ " c where c.publication.id=:pubId and c.state!=:st and c.deletionDate is null");
		query.setParameter("pubId", chapterId);
		query.setParameter("st", CommentState.REJECTED);
		return query.getResultList();
	}

	public List<Comment> listForPublicationAndDomId(Long publicationId,
			String domId) {
		if (publicationId == null) {
			throw new IllegalArgumentException("Publication id cannot be null.");
		}
		Query query = this.entityManager
				.createQuery("from "
						+ getEntityType()
						+ " c where c.publication.id=:pubId and c.state!=:st and c.domId=:cid and c.deletionDate is null");
		query.setParameter("pubId", publicationId);
		query.setParameter("st", CommentState.REJECTED);
		query.setParameter("cid", domId);

		return query.getResultList();
	}

	public List<Object[]> listCommentsInforForPublication(Long publicationId) {
		if (publicationId == null) {
			throw new IllegalArgumentException("Chapter id cannot be null.");
		}
		String queryStr = String
				.format(
						"select c.domId, count(c.domId) from %s c where c.publication.id=:id and c.deletionDate is null group by c.domId",
						Comment.class.getName());
		Query query = this.entityManager.createQuery(queryStr);
		query.setParameter("id", publicationId);
		return query.getResultList();
	}

}
