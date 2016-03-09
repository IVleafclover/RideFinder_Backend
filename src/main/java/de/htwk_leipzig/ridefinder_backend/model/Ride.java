package de.htwk_leipzig.ridefinder_backend.model;

/**
 * repraesentiert eine Mitfahrgelegenheit
 *
 * @author Christian
 *
 */
public class Ride {

	/**
	 * von
	 */
	private String from;

	/**
	 * zu
	 */
	private String to;

	/**
	 * Uhrzeit
	 */
	private String time;

	/**
	 * Preis
	 */
	private Float price;

	/**
	 * freie Plaetze
	 */
	private int seat;

	/**
	 * Datum
	 */
	private String date;

	/**
	 * Link zu externer Seite
	 */
	private String link;

	/**
	 * Provider der Mitfahrgelegenheit
	 */
	private String provider;

	/**
	 * Konstruktor
	 */
	public Ride() {
		super();
	}

	/**
	 * Konstruktor
	 *
	 * @param from
	 * @param to
	 * @param time
	 * @param price
	 * @param seat
	 * @param date
	 * @param link
	 * @param provider
	 */
	public Ride(final String from, final String to, final String time, final Float price, final int seat,
			final String date, final String link, final String provider) {
		super();
		this.from = from;
		this.to = to;
		this.time = time;
		this.price = price;
		this.seat = seat;
		this.date = date;
		this.link = link;
		this.provider = provider;
	}

	/**
	 * @return von
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @param from
	 */
	public void setFrom(final String from) {
		this.from = from;
	}

	/**
	 * @return zu
	 */
	public String getTo() {
		return to;
	}

	/**
	 * @param to
	 */
	public void setTo(final String to) {
		this.to = to;
	}

	/**
	 * @return Uhrzeit
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time
	 */
	public void setTime(final String time) {
		this.time = time;
	}

	/**
	 * @return Preis
	 */
	public Float getPrice() {
		return price;
	}

	/**
	 * @param price
	 */
	public void setPrice(final Float price) {
		this.price = price;
	}

	/**
	 * @return freie Plaetze
	 */
	public int getSeat() {
		return seat;
	}

	/**
	 * @param seat
	 */
	public void setSeat(final int seat) {
		this.seat = seat;
	}

	/**
	 * @return Datum
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date
	 */
	public void setDate(final String date) {
		this.date = date;
	}

	/**
	 * @return Link
	 */
	public String getLink() {
		return link;
	}

	/**
	 * @param link
	 */
	public void setLink(final String link) {
		this.link = link;
	}

	/**
	 * @return Provider
	 */
	public String getProvider() {
		return provider;
	}

	/**
	 * @param provider
	 */
	public void setProvider(final String provider) {
		this.provider = provider;
	}

	@Override
	public String toString() {
		return "Ride [id=" + getId() + ", from=" + from + ", to=" + to + ", time=" + time + ", price=" + price
				+ ", seat=" + seat + ", date=" + date + ", link=" + link + ", provider=" + provider + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((price == null) ? 0 : price.hashCode());
		result = prime * result + ((provider == null) ? 0 : provider.hashCode());
		result = prime * result + seat;
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Ride other = (Ride) obj;
		if (date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!date.equals(other.date)) {
			return false;
		}
		if (from == null) {
			if (other.from != null) {
				return false;
			}
		} else if (!from.equals(other.from)) {
			return false;
		}
		if (link == null) {
			if (other.link != null) {
				return false;
			}
		} else if (!link.equals(other.link)) {
			return false;
		}
		if (price == null) {
			if (other.price != null) {
				return false;
			}
		} else if (!price.equals(other.price)) {
			return false;
		}
		if (provider == null) {
			if (other.provider != null) {
				return false;
			}
		} else if (!provider.equals(other.provider)) {
			return false;
		}
		if (seat != other.seat) {
			return false;
		}
		if (time == null) {
			if (other.time != null) {
				return false;
			}
		} else if (!time.equals(other.time)) {
			return false;
		}
		if (to == null) {
			if (other.to != null) {
				return false;
			}
		} else if (!to.equals(other.to)) {
			return false;
		}
		return true;
	}

	/**
	 * gibt die Id des Suchergebnisses wieder
	 *
	 * @return Id
	 */
	public String getId() {
		return getLink() + "-" + getDate();
	}
}
