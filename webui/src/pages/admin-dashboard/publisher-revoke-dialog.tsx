/********************************************************************************
 * Copyright (c) 2020 TypeFox and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

import React, { FunctionComponent, useState, useContext } from 'react';
import { Button, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Typography } from '@material-ui/core';
import { PublisherInfo, isError } from '../../extension-registry-types';
import { ServiceContext } from '../../default/default-app';
import { ErrorHandlerContext } from '../../main';
import { UpdateContext } from './publisher-admin';

interface PublisherRevokeDialogProps {
    publisherInfo: PublisherInfo;
}

export const PublisherRevokeDialog: FunctionComponent<PublisherRevokeDialogProps> = props => {
    const service = useContext(ServiceContext);
    const errorContext = useContext(ErrorHandlerContext);

    const updateContext = useContext(UpdateContext);

    const [dialogOpen, setDialogOpen] = useState(false);
    const handleOpenRevokeDialog = () => {
        setDialogOpen(true);
    };
    const handleCancelRevokeDialog = () => {
        setDialogOpen(false);
    };
    const handleRemoveVersions = async () => {
        try {
            updateContext.setLoading(true);
            const result = await service.revokePublishersAgreement(props.publisherInfo.user.provider!, props.publisherInfo.user.loginName);
            if (isError(result)) {
                throw (result.error);
            }
            updateContext.handleUpdate();
            updateContext.setLoading(false);
            setDialogOpen(false);
        } catch (err) {
            errorContext && errorContext.handleError(err);
        }
    };

    return <>
        <Button variant='contained' color='secondary' onClick={handleOpenRevokeDialog} disabled={!props.publisherInfo.accessTokens.find(a => a.active)}>
            Revoke Publishers Agreement
        </Button>
        <Dialog
            open={dialogOpen}
            onClose={handleCancelRevokeDialog}>
            <DialogTitle >Revoke Publishers Agreement?</DialogTitle>
            <DialogContent>
                <DialogContentText component='div'>
                    <Typography>
                        This will inactivate {props.publisherInfo.user.loginName}s access
                        tokens and delete the extensions published by {props.publisherInfo.user.loginName}.
                    </Typography>
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button autoFocus onClick={handleCancelRevokeDialog} variant='contained' color='primary'>
                    Cancel
                </Button>
                <Button onClick={handleRemoveVersions} variant='contained' color='secondary' autoFocus>
                    Revoke Agreement
                </Button>
            </DialogActions>
        </Dialog>
    </>;
};