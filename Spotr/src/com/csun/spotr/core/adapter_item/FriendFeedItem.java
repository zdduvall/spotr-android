package com.csun.spotr.core.adapter_item;

import com.csun.spotr.core.Challenge;
import com.csun.spotr.core.Comment;

public class FriendFeedItem {
	// required parameters
	private final int activityId;
	private final int friendId;
	private String friendName;

	private final Challenge.Type challengeType;
	private final String activityTime;
	private final String placeName;

	// optional parameter
	private String challengeName;
	private String challengeDescription;
	private String activitySnapPictureUrl = null;
	private String friendPictureUrl = null;
	private String activityComment;
	private String shareUrl = "";
	private int likes;
	private int numberOfComments;
	private Comment firstComment;
	private String treasureIconUrl;
	private String treasureCompany;

	public static class Builder {
		// required parameters
		private final int activityId;
		private final int friendId;
		private final String friendName;
		private final Challenge.Type challengeType;
		private final String activityTime;
		private final String placeName;

		// optional parameter
		private String challengeName;
		private String challengeDescription;
		private String activitySnapPictureUrl = null;
		private String friendPictureUrl = null;
		private String activityComment;
		private String shareUrl = "";
		private int likes;
		private int numberOfComments;
		private Comment firstComment;
		private String treasureIconUrl;
		private String treasureCompany;

		public Builder(int activityId, int friendId, String friendName, Challenge.Type challengeType, String activityTime, String placeName) {
			this.activityId = activityId;
			this.friendId = friendId;
			this.friendName = friendName;
			this.challengeType = challengeType;
			this.activityTime = activityTime;
			this.placeName = placeName;
		}

		public Builder challengeName(String challengeName) {
			this.challengeName = challengeName;
			return this;
		}

		public Builder challengeDescription(String challengeDescription) {
			this.challengeDescription = challengeDescription;
			return this;
		}

		public Builder activitySnapPictureUrl(String url) {
			this.activitySnapPictureUrl = url;
			return this;
		}

		public Builder friendPictureUrl(String url) {
			this.friendPictureUrl = url;
			return this;
		}

		public Builder activityComment(String activityComment) {
			this.activityComment = activityComment;
			return this;
		}

		public Builder shareUrl(String shareUrl) {
			this.shareUrl = shareUrl;
			return this;
		}

		public Builder likes(int likes) {
			this.likes = likes;
			return this;
		}

		public Builder numberOfComments(int numberOfComments) {
			this.numberOfComments = numberOfComments;
			return this;
		}

		public Builder firstComment(Comment firstComment) {
			this.firstComment = firstComment;
			return this;
		}

		public Builder treasureIconUrl(String treasureIconUrl) {
			this.treasureIconUrl = treasureIconUrl;
			return this;
		}

		public Builder treasureCompany(String treasureCompany) {
			this.treasureCompany = treasureCompany;
			return this;
		}

		public FriendFeedItem build() {
			return new FriendFeedItem(this);
		}
	}

	public FriendFeedItem(Builder builder) {
		this.activityId = builder.activityId;
		this.friendId = builder.friendId;
		this.friendName = builder.friendName;
		this.challengeType = builder.challengeType;
		this.activityTime = builder.activityTime;
		this.placeName = builder.placeName;

		this.challengeName = builder.challengeName;
		this.challengeDescription = builder.challengeDescription;
		this.activitySnapPictureUrl = builder.activitySnapPictureUrl;
		this.friendPictureUrl = builder.friendPictureUrl;
		this.activityComment = builder.activityComment;
		this.shareUrl = builder.shareUrl;
		this.likes = builder.likes;
		this.numberOfComments = builder.numberOfComments;
		this.firstComment = builder.firstComment;
		this.treasureIconUrl = builder.treasureIconUrl;
		this.treasureCompany = builder.treasureCompany;
	}

	public int getNumberOfComments() {
		return numberOfComments;
	}

	public void setNumberOfComments(int numberOfComments) {
		this.numberOfComments = numberOfComments;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public String getShareUrl() {
		return shareUrl;
	}

	public void setShareUrl(String shareUrl) {
		this.shareUrl = shareUrl;
	}

	public String getChallengeName() {
		return challengeName;
	}

	public void setChallengeName(String challengeName) {
		this.challengeName = challengeName;
	}

	public String getChallengeDescription() {
		return challengeDescription;
	}

	public void setChallengeDescription(String challengeDescription) {
		this.challengeDescription = challengeDescription;
	}

	public String getActivitySnapPictureUrl() {
		return activitySnapPictureUrl;
	}

	public void setActivitySnapPictureUrl(String activitySnapPictureUrl) {
		this.activitySnapPictureUrl = activitySnapPictureUrl;
	}

	public String getFriendPictureUrl() {
		return friendPictureUrl;
	}

	public void setFriendPictureUrl(String url) {
		this.friendPictureUrl = url;
	}

	public String getActivityComment() {
		return activityComment;
	}

	public void setActivityComment(String activityComment) {
		this.activityComment = activityComment;
	}

	public int getActivityId() {
		return activityId;
	}

	public int getFriendId() {
		return friendId;
	}

	public String getFriendName() {
		return friendName;
	}

	public void setFriendName(String friendName) {
		this.friendName = friendName;
	}

	public Challenge.Type getChallengeType() {
		return challengeType;
	}

	public String getActivityTime() {
		return activityTime;
	}

	public String getPlaceName() {
		return placeName;
	}

	public Comment getFirstComment() {
		return firstComment;
	}

	public void setFirstComment(Comment firstComment) {
		this.firstComment = firstComment;
	}

	public String getTreasureIconUrl() {
		return treasureIconUrl;
	}

	public void setTreasureIconUrl(String treasureIconUrl) {
		this.treasureIconUrl = treasureIconUrl;
	}

	public String getTreasureCompany() {
		return treasureCompany;
	}

	public void setTreasureCompany(String treasureCompany) {
		this.treasureCompany = treasureCompany;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + activityId;
		result = prime * result + ((activityTime == null) ? 0 : activityTime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FriendFeedItem other = (FriendFeedItem) obj;
		if (activityId != other.activityId)
			return false;
		if (activityTime == null) {
			if (other.activityTime != null)
				return false;
		}
		else if (!activityTime.equals(other.activityTime))
			return false;
		return true;
	}
}