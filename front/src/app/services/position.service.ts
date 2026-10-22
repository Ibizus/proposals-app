import { Injectable } from '@angular/core';
import { HttpClient, HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Position } from '../utils/types';

const POSITIONS_URL = environment.API_ENDPOINT + 'positions/';

export interface PaginatedPositions {
  positions: Position[];
  currentPage: number;
  totalItems: number;
  totalPages: number;
}

@Injectable({
  providedIn: 'root',
})
export class PositionService {
  constructor(private http: HttpClient) {}

  /** Fetches a paginated page of positions. */
  getPositions(page: number, size: number): Observable<PaginatedPositions> {
    return this.http.get<PaginatedPositions>(
      POSITIONS_URL + '?page=' + page + '&size=' + Math.ceil(size),
    );
  }

  /** Fetches every position, used to fill the position pickers. */
  getAllPositions(): Observable<Position[]> {
    return this.http.get<Position[]>(POSITIONS_URL);
  }

  getPosition(id: number, context?: HttpContext): Observable<Position> {
    return this.http.get<Position>(POSITIONS_URL + id, { context });
  }

  createPosition(position: Position, context?: HttpContext): Observable<unknown> {
    return this.http.post(POSITIONS_URL, position, { context });
  }

  modifyPosition(position: Position, context?: HttpContext): Observable<unknown> {
    return this.http.put(POSITIONS_URL + position.id, position, { context });
  }

  deletePosition(id: number, context?: HttpContext): Observable<unknown> {
    return this.http.delete(POSITIONS_URL + id, { context });
  }
}
