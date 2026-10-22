import { Routes } from '@angular/router';
import { LoginFormComponent } from './components/login-form/login-form.component';
import { ProposalsComponent } from './pages/proposals/proposals.component';
import { NewProposalComponent } from './pages/new-proposal/new-proposal.component';
import { DetailedProposalComponent } from './pages/detailed-proposal/detailed-proposal.component';
import { CandidatesComponent } from './pages/candidates/candidates.component';
import { PositionsComponent } from './pages/positions/positions.component';
import { TaxesComponent } from './pages/taxes/taxes.component';
import { ExtrasComponent } from './pages/extras/extras.component';
import { ParametersComponent } from './pages/parameters/parameters.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';

export const routes: Routes = [
  {
    path: '',
    component: LoginFormComponent,
    title: 'PropFlow',
  },
  {
    path: 'proposals',
    component: ProposalsComponent,
    title: 'Proposals | PropFlow',
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    title: 'Dashboard | PropFlow',
  },
  {
    path: 'new-proposal',
    component: NewProposalComponent,
    title: 'New Proposal | PropFlow',
  },
  {
    path: 'proposal/:id',
    component: DetailedProposalComponent,
    title: 'Proposal Details | PropFlow',
  },
  {
    path: 'candidates',
    component: CandidatesComponent,
    title: 'Candidates | PropFlow',
  },
  {
    path: 'positions',
    component: PositionsComponent,
    title: 'Positions | PropFlow',
  },
  {
    path: 'taxes',
    component: TaxesComponent,
    title: 'Portugal Taxes | PropFlow',
  },
  {
    path: 'extras',
    component: ExtrasComponent,
    title: 'Extras | PropFlow',
  },
  {
    path: 'parameters',
    component: ParametersComponent,
    title: 'Parameters | PropFlow',
  },
  {
    path: '**',
    pathMatch: 'full',
    redirectTo: '',
  },
];
