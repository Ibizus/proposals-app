import { Candidate, Extra, Office, Taxes } from './types';

async function getCandidates(url: string): Promise<Candidate[]> {
  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const data = await response.json(); // Parse JSON data
    return data as Candidate[];
  } catch (error) {
    console.error('Error fetching data:', error);
    return [];
  }
}

async function getExtras(url: string): Promise<Extra[]> {
  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const data = await response.json();
    return data as Extra[];
  } catch (error) {
    console.error('Error fetching data: ', error);
    return [];
  }
}

async function getOffices(url: string): Promise<Office[]> {
  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const data = await response.json();
    return data as Office[];
  } catch (error) {
    console.error('Error fetching data: ', error);
    return [];
  }
}

async function getTaxes(url: string): Promise<Taxes[]> {
  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const data = await response.json();
    return data as Taxes[];
  } catch (error) {
    console.error('Error fetching data: ', error);
    return [];
  }
}

function sleep(ms: number | undefined) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export { getCandidates, getExtras, getOffices, getTaxes, sleep };
