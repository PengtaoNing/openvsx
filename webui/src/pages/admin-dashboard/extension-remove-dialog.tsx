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
import { Extension } from '../../extension-registry-types';
import { ServiceContext } from '../../default/default-app';
import { ErrorHandlerContext } from '../../main';

interface ExtensionRemoveDialogProps {
    versions: string[];
    extension: Extension;
    onUpdate: () => void;
}

export const ExtensionRemoveDialog: FunctionComponent<ExtensionRemoveDialogProps> = props => {
    const service = useContext(ServiceContext);
    const errorContext = useContext(ErrorHandlerContext);

    const [dialogOpen, setDialogOpen] = useState(false);
    const handleOpenRemoveDialog = () => {
        setDialogOpen(true);
    };
    const handleCancelRemoveDialog = () => {
        setDialogOpen(false);
    };
    const handleRemoveVersions = async () => {
        try {
            const prms = props.versions.map(version => service.deleteExtensionVersion({ version, extension: props.extension.name, namespace: props.extension.namespace }));
            await Promise.all(prms);
            props.onUpdate();
            setDialogOpen(false);
        } catch (err) {
            errorContext && errorContext.handleError(err);
        }
    };

    return <>
        <Button variant='contained' color='secondary' onClick={handleOpenRemoveDialog} disabled={!props.versions.length}>
            Remove version{props.versions.length > 1 ? 's' : ''}
        </Button>
        <Dialog
            open={dialogOpen}
            onClose={handleCancelRemoveDialog}>
            <DialogTitle >Remove {props.versions.length} version{props.versions.length > 1 ? 's' : ''} of {props.extension.name}?</DialogTitle>
            <DialogContent>
                <DialogContentText component='div'>
                    {props.versions.map((version, key) => <Typography key={key} variant='body2'>{version}</Typography>)}
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button autoFocus onClick={handleCancelRemoveDialog} variant='contained' color='primary'>
                    Cancel
                </Button>
                <Button onClick={handleRemoveVersions} variant='contained' color='secondary' autoFocus>
                    Remove
                </Button>
            </DialogActions>
        </Dialog>
    </>;
};