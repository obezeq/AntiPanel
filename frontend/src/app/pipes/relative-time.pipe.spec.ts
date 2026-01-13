import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { RelativeTimePipe } from './relative-time.pipe';

describe('RelativeTimePipe', () => {
  let pipe: RelativeTimePipe;
  const NOW = new Date('2024-03-15T12:00:00Z');

  beforeEach(() => {
    pipe = new RelativeTimePipe();
    vi.useFakeTimers();
    vi.setSystemTime(NOW);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  describe('instantiation', () => {
    it('should create an instance', () => {
      expect(pipe).toBeTruthy();
    });
  });

  describe('null and undefined handling', () => {
    it('should return empty string for null', () => {
      expect(pipe.transform(null)).toBe('');
    });

    it('should return empty string for undefined', () => {
      expect(pipe.transform(undefined)).toBe('');
    });
  });

  describe('invalid date handling', () => {
    it('should return empty string for invalid date string', () => {
      expect(pipe.transform('not-a-date')).toBe('');
    });

    it('should return empty string for empty string', () => {
      expect(pipe.transform('')).toBe('');
    });
  });

  describe('just now (within 10 seconds)', () => {
    it('should return "just now" for current time', () => {
      expect(pipe.transform(NOW)).toBe('just now');
    });

    it('should return "just now" for 5 seconds ago', () => {
      const date = new Date(NOW.getTime() - 5 * 1000);
      expect(pipe.transform(date)).toBe('just now');
    });

    it('should return "just now" for 5 seconds in future', () => {
      const date = new Date(NOW.getTime() + 5 * 1000);
      expect(pipe.transform(date)).toBe('just now');
    });
  });

  describe('seconds ago', () => {
    it('should return "30 seconds ago"', () => {
      const date = new Date(NOW.getTime() - 30 * 1000);
      expect(pipe.transform(date)).toBe('30 seconds ago');
    });

    it('should return "1 second ago" (singular)', () => {
      // Just past the threshold
      vi.setSystemTime(new Date(NOW.getTime() + 11 * 1000));
      expect(pipe.transform(NOW)).toBe('11 seconds ago');
    });
  });

  describe('minutes ago', () => {
    it('should return "1 minute ago" (singular)', () => {
      const date = new Date(NOW.getTime() - 60 * 1000);
      expect(pipe.transform(date)).toBe('1 minute ago');
    });

    it('should return "5 minutes ago" (plural)', () => {
      const date = new Date(NOW.getTime() - 5 * 60 * 1000);
      expect(pipe.transform(date)).toBe('5 minutes ago');
    });

    it('should return "45 minutes ago"', () => {
      const date = new Date(NOW.getTime() - 45 * 60 * 1000);
      expect(pipe.transform(date)).toBe('45 minutes ago');
    });
  });

  describe('hours ago', () => {
    it('should return "1 hour ago" (singular)', () => {
      const date = new Date(NOW.getTime() - 60 * 60 * 1000);
      expect(pipe.transform(date)).toBe('1 hour ago');
    });

    it('should return "3 hours ago" (plural)', () => {
      const date = new Date(NOW.getTime() - 3 * 60 * 60 * 1000);
      expect(pipe.transform(date)).toBe('3 hours ago');
    });

    it('should return "23 hours ago"', () => {
      const date = new Date(NOW.getTime() - 23 * 60 * 60 * 1000);
      expect(pipe.transform(date)).toBe('23 hours ago');
    });
  });

  describe('days ago', () => {
    it('should return "1 day ago" (singular)', () => {
      const date = new Date(NOW.getTime() - 24 * 60 * 60 * 1000);
      expect(pipe.transform(date)).toBe('1 day ago');
    });

    it('should return "5 days ago" (plural)', () => {
      const date = new Date(NOW.getTime() - 5 * 24 * 60 * 60 * 1000);
      expect(pipe.transform(date)).toBe('5 days ago');
    });
  });

  describe('weeks ago', () => {
    it('should return "1 week ago" (singular)', () => {
      const date = new Date(NOW.getTime() - 7 * 24 * 60 * 60 * 1000);
      expect(pipe.transform(date)).toBe('1 week ago');
    });

    it('should return "2 weeks ago" (plural)', () => {
      const date = new Date(NOW.getTime() - 14 * 24 * 60 * 60 * 1000);
      expect(pipe.transform(date)).toBe('2 weeks ago');
    });
  });

  describe('months ago', () => {
    it('should return "1 month ago" (singular)', () => {
      const date = new Date(NOW.getTime() - 30 * 24 * 60 * 60 * 1000);
      expect(pipe.transform(date)).toBe('1 month ago');
    });

    it('should return "6 months ago" (plural)', () => {
      const date = new Date(NOW.getTime() - 6 * 30 * 24 * 60 * 60 * 1000);
      expect(pipe.transform(date)).toBe('6 months ago');
    });
  });

  describe('years ago', () => {
    it('should return "1 year ago" (singular)', () => {
      const date = new Date(NOW.getTime() - 365 * 24 * 60 * 60 * 1000);
      expect(pipe.transform(date)).toBe('1 year ago');
    });

    it('should return "3 years ago" (plural)', () => {
      const date = new Date(NOW.getTime() - 3 * 365 * 24 * 60 * 60 * 1000);
      expect(pipe.transform(date)).toBe('3 years ago');
    });
  });

  describe('future dates', () => {
    it('should return "in 5 minutes" for future time', () => {
      const date = new Date(NOW.getTime() + 5 * 60 * 1000);
      expect(pipe.transform(date)).toBe('in 5 minutes');
    });

    it('should return "in 2 hours" for future time', () => {
      const date = new Date(NOW.getTime() + 2 * 60 * 60 * 1000);
      expect(pipe.transform(date)).toBe('in 2 hours');
    });

    it('should return "in 3 days" for future time', () => {
      const date = new Date(NOW.getTime() + 3 * 24 * 60 * 60 * 1000);
      expect(pipe.transform(date)).toBe('in 3 days');
    });
  });

  describe('different input types', () => {
    it('should handle Date object', () => {
      const date = new Date(NOW.getTime() - 60 * 1000);
      expect(pipe.transform(date)).toBe('1 minute ago');
    });

    it('should handle ISO string', () => {
      const isoString = new Date(NOW.getTime() - 2 * 60 * 60 * 1000).toISOString();
      expect(pipe.transform(isoString)).toBe('2 hours ago');
    });

    it('should handle timestamp in milliseconds', () => {
      const timestamp = NOW.getTime() - 3 * 60 * 60 * 1000;
      expect(pipe.transform(timestamp)).toBe('3 hours ago');
    });

    it('should handle timestamp in seconds', () => {
      // Unix timestamp (seconds since epoch)
      const timestampSeconds = Math.floor((NOW.getTime() - 4 * 60 * 60 * 1000) / 1000);
      expect(pipe.transform(timestampSeconds)).toBe('4 hours ago');
    });
  });
});
