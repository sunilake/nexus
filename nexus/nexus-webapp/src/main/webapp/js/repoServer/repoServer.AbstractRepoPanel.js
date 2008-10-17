/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
/*
 * Repository panel superclass
 */

/* config options:
  {
    id: the is of this panel instance [required]
    title: title of this panel (shows in tab)
  }
*/
Sonatype.repoServer.AbstractRepoPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);
  
  this.sp = Sonatype.lib.Permissions;

  this.ctxRecord = null;
  this.reposGridPanel == null;
  
  this.repoActions = {
    clearCache: {
      text: 'Clear Cache',
      handler: this.clearCacheHandler
    },
    reIndex: {
      text: 'Re-Index',
      handler: this.reIndexHandler
    },
    rebuildAttributes: {
      text: 'Rebuild Attributes',
      handler: this.rebuildAttributesHandler
    },
    uploadArtifact: {
      text: 'Upload Artifact...',
      handler: this.uploadArtifactHandler
    }
  };

  Sonatype.repoServer.AbstractRepoPanel.superclass.constructor.call(this, {
  });
};

Ext.extend(Sonatype.repoServer.AbstractRepoPanel, Ext.Panel, {
  hasSelection: function() {
    return this.ctxRecord || this.reposGridPanel.getSelectionModel().hasSelection();
  },

  viewHandler : function(){
    if (this.ctxRecord || this.reposGridPanel.getSelectionModel().hasSelection()){
      var rec = (this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected();
      this.viewRepo(rec);
    }
  },
  
  clearCacheHandler: function( rec ) {
    var url = Sonatype.config.repos.urls.cache + rec.id.slice(
      Sonatype.config.host.length + Sonatype.config.servicePath.length );
    
    //make sure to provide /content path for repository root requests like ../repositories/central
    if (/.*\/repositories\/[^\/]*$/i.test(url) || /.*\/repo_groups\/[^\/]*$/i.test(url)){
      url += '/content';
    }
    
    Ext.Ajax.request({
      url: url,
      callback: this.clearCacheCallback,
      scope: this,
      method: 'DELETE'
    });
  },
  
  clearCacheCallback : function(options, isSuccess, response){
    //@todo: stop updating messaging here
    if(isSuccess){
      
    }
    else {
      Sonatype.utils.connectionError( response, 'The server did not clear the repository\'s cache.' );
    }
  },

  
  reIndexHandler: function( rec ){
    var url = Sonatype.config.repos.urls.index +
      rec.id.slice(Sonatype.config.host.length + Sonatype.config.servicePath.length);
    
    //make sure to provide /content path for repository root requests like ../repositories/central
    if (/.*\/repositories\/[^\/]*$/i.test(url) || /.*\/repo_groups\/[^\/]*$/i.test(url)){
      url += '/content';
    }
    
    Ext.Ajax.request({
      url: url,
      callback: this.reIndexCallback,
      scope: this,
      method: 'DELETE'
    });
  },
  
  reIndexCallback : function(options, isSuccess, response){
    //@todo: stop updating messaging here
    if(isSuccess){

    }
    else {
      Sonatype.utils.connectionError( response, 'The server did not re-index the repository.' );
    }
  },
  
  rebuildAttributesHandler: function( rec ){
    var url = Sonatype.config.repos.urls.attributes +
      rec.id.slice(Sonatype.config.host.length + Sonatype.config.servicePath.length);
    
    //make sure to provide /content path for repository root requests like ../repositories/central
    if (/.*\/repositories\/[^\/]*$/i.test(url) || /.*\/repo_groups\/[^\/]*$/i.test(url)){
      url += '/content';
    }
    
    Ext.Ajax.request({
      url: url,
      callback: this.rebuildAttributesCallback,
      scope: this,
      method: 'DELETE'
    });
  },
  
  rebuildAttributesCallback : function(options, isSuccess, response){
    //@todo: stop updating messaging here
    if(isSuccess){

    }
    else {
      Sonatype.utils.connectionError( response, 'The server did not rebuild attributes in the repository.' );
    }
  },
  
  putInServiceHandler : function(){
    if (this.hasSelection()){
      //@todo: start updating messaging here
      var rec = (this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected();
      
      var out = {
        data : {
          id : rec.id.slice(rec.id.lastIndexOf('/') + 1),
          repoType : rec.get('repoType'),
          localStatus : 'inService'
        }
      };
      
      Ext.Ajax.request({
        url: rec.id + '/status',
        jsonData: out,
        callback: this.putInServiceCallback,
        scope: this,
        method: 'PUT'
      });
    }
  },
  
  putInServiceCallback : function(options, isSuccess, response){
    //@todo: stop updating messaging here
    if(isSuccess){
      var statusResp = Ext.decode(response.responseText);
      this.updateRepoStatuses(statusResp.data);
    }
    else {
      Sonatype.utils.connectionError( response, 'The server did not put the repository into service.' );
    }
  },

  putOutOfServiceHandler : function(){
    if (this.hasSelection()){
      //@todo: start updating messaging here
      var rec = (this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected();
      
      var out = {
        data : {
          id : rec.id.slice(rec.id.lastIndexOf('/') + 1),
          repoType : rec.get('repoType'),
          localStatus : 'outOfService'
        }
      };
      
      Ext.Ajax.request({
        url: rec.id + '/status',
        jsonData: out,
        callback: this.putOutOfServiceCallback,
        scope: this,
        method: 'PUT'
      });
    }
  },
  
  allowProxyHandler : function(){
    if (this.hasSelection()){
      //@todo: start updating messaging here
      var rec = (this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected();
      
      var out = {
        data : {
          id : rec.id.slice(rec.id.lastIndexOf('/') + 1),
          repoType : rec.get('repoType'),
          localStatus : rec.get('localStatus'),
          remoteStatus : rec.get('remoteStatus'),
          proxyMode : 'allow'
        }
      };
      
      Ext.Ajax.request({
        url: rec.id + '/status',
        jsonData: out,
        callback: this.allowProxyCallback,
        scope: this,
        method: 'PUT'
      });
    }
  },
  
  allowProxyCallback : function(options, isSuccess, response){
    //@todo: stop updating messaging here
    if(isSuccess){
      var statusResp = Ext.decode(response.responseText);
      this.updateRepoStatuses(statusResp.data);
    }
    else {
      Sonatype.utils.connectionError( response, 'The server did not update the proxy repository status to allow.' );
    }
  },
  
  blockProxyHandler : function(){
    if (this.hasSelection()){
      //@todo: start updating messaging here
      var rec = (this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected();
      
      var out = {
        data : {
          id : rec.id.slice(rec.id.lastIndexOf('/') + 1),
          repoType : rec.get('repoType'),
          localStatus : rec.get('localStatus'),
          remoteStatus : rec.get('remoteStatus'),
          proxyMode : 'blockedManual'
        }
      };
      
      Ext.Ajax.request({
        url: rec.id + '/status',
        jsonData: out,
        callback: this.blockProxyCallback,
        scope: this,
        method: 'PUT'
      });
    }
  },
  
  blockProxyCallback : function(options, isSuccess, response){
    //@todo: stop updating messaging here
    if(isSuccess){
      var statusResp = Ext.decode(response.responseText);
      this.updateRepoStatuses(statusResp.data);
    }
    else {
      Sonatype.utils.connectionError( response, 'The server did not update the proxy repository status to blocked.' );
    }
  },
  
  putOutOfServiceCallback : function(options, isSuccess, response){
    //@todo: stop updating messaging here
    if(isSuccess){
      var statusResp = Ext.decode(response.responseText);
      this.updateRepoStatuses(statusResp.data);
    }
    else {
      Sonatype.utils.connectionError( response, 'The server did not put the repository out of service.' );
    }
  },
  
  updateRepoStatuses: function(data) {
  },
  
  uploadArtifactHandler : function( rec ){
      
    Ext.Ajax.request({
      url: rec.id,
      scope: this,
      callback: function(options, success, response) {
        if ( success ) {
          var statusResp = Ext.decode(response.responseText);
          if (statusResp.data) {
            if ( statusResp.data.allowWrite ) {
              var oldItem = this.formCards.getLayout().activeItem;
              this.formCards.remove(oldItem, true);
              
              if ( this.formCards.tbar ) {
                this.formCards.tbar.oldSize = this.formCards.tbar.getSize(); 
                this.formCards.tbar.hide();
                this.formCards.tbar.setHeight(0);
              }

//              var rec = (this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected();

              var panel = new Ext.Panel({
                layout: 'fit',
                frame: true,
                items: [ new Sonatype.repoServer.FileUploadPanel({
                  title: 'Artifact Upload to ' + rec.get('name'),
                  repoPanel: this,
                  repoRecord: rec
                }) ]
              });
              this.formCards.insert(1, panel);
              this.formCards.getLayout().setActiveItem(panel);
              panel.doLayout();
            }
            else {
              Sonatype.MessageBox.show({
                title: 'Deployment Disabled',
                icon: Sonatype.MessageBox.ERROR,
                buttons: Sonatype.MessageBox.OK,
                msg: 'Deployment is disabled for the selected repository.<br /><br />' +
                  'You can enable it in the "Access Settings" section of the repository configuration'
              });
            }
            return;
          }
        }
        Sonatype.utils.connectionError( response, 'There was a problem obtaining repository status.' );
      }
    });
  },
  
  onRepositoryMenuInit: function( menu, repoRecord ) {
    if ( this.sp.checkPermission(
          Sonatype.user.curr.repoServer.actionDeleteCache, this.sp.DELETE ) &&
        repoRecord.get( 'repoType' ) != 'virtual' ) {
      menu.add( this.repoActions.clearCache );
    }

    if ( this.sp.checkPermission(
          Sonatype.user.curr.repoServer.actionReindex, this.sp.DELETE ) ) {
      menu.add( this.repoActions.reIndex );
    }

    if ( this.sp.checkPermission(
          Sonatype.user.curr.repoServer.actionRebuildAttribs, this.sp.DELETE ) ){
      menu.add( this.repoActions.rebuildAttributes );
    }

    if ( this.sp.checkPermission(
          Sonatype.user.curr.repoServer.actionUploadArtifact, this.sp.CREATE ) &&
        repoRecord.get('repoType') == 'hosted' &&
        repoRecord.get('repoPolicy') == 'release' ){
      menu.add( this.repoActions.uploadArtifact );
    }
  }
});

Sonatype.repoServer.DefaultRepoHandler = new Sonatype.repoServer.AbstractRepoPanel();
